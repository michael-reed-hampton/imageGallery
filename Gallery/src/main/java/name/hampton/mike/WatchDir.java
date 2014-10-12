package name.hampton.mike;

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;

import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public abstract class WatchDir {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;

	private boolean runprocess = false;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
            	logger.debug(String.format("register: %s\n", dir));
            } else {
                if (!dir.equals(prev)) {
                	logger.debug(String.format("update: %s -> %s\n", prev, dir));
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;

        if (recursive) {
        	logger.debug(String.format("Scanning %s ...\n", dir));
            registerAll(dir);
            logger.debug(String.format("Done."));
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
        while(runprocess ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
            	logger.error(String.format("WatchKey not recognized!! %s ", key));
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                Kind<?> kind = event.kind();

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    handleOverflow(ev, child);
                }

                // print out event                
                if(kind == ENTRY_MODIFY) {
                	handleModify(ev, child);
                	// if( System.currentTimeMillis()-child.toFile().lastModified() < 1000)
                	// 	System.out.format("%s: %s %s\n", event.kind().name(), child, new Date(child.toFile().lastModified()).toString() );
                }
                else 
                	logger.debug(String.format("%s: %s %s\n", event.kind().name(), child, new Date(child.toFile().lastModified()).toString()));
                
                if(kind == ENTRY_DELETE){
                	handleDelete(ev, child);
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                            handleCreate(ev, child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    public boolean isRunprocess() {
		return runprocess;
	}

	public void setRunprocess(boolean runprocess) {
		this.runprocess = runprocess;
	}

	public abstract void handleDelete(WatchEvent<Path> event, Path child);

	public abstract void handleCreate(WatchEvent<Path> event, Path child);

	public abstract void handleModify(WatchEvent<Path> event, Path child);

	public abstract void handleOverflow(WatchEvent<Path> event, Path child);

	static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
        if (args.length == 0 || args.length > 2)
            usage();
        boolean recursive = false;
        int dirArg = 0;
        if (args[0].equals("-r")) {
            if (args.length < 2)
                usage();
            recursive = true;
            dirArg++;
        }

        // register directory and process its events
        Path dir = Paths.get(args[dirArg]);
        WatchDir watchdir = new WatchDir(dir, recursive){

			@Override
			public void handleDelete(WatchEvent<Path> event, Path child) {
        	 	System.out.format("%s: %s %s\n", event.kind().name(), child, new Date(child.toFile().lastModified()).toString() );
			}

			@Override
			public void handleCreate(WatchEvent<Path> event, Path child) {
        	 	System.out.format("%s: %s %s\n", event.kind().name(), child, new Date(child.toFile().lastModified()).toString() );
			}

			@Override
			public void handleModify(WatchEvent<Path> event, Path child) {
            	if( System.currentTimeMillis()-child.toFile().lastModified() < 1000){
            	 	System.out.format("%s: %s %s\n", event.kind().name(), child, new Date(child.toFile().lastModified()).toString() );
            	}
			}

			@Override
			public void handleOverflow(WatchEvent<Path> event, Path child) {
        	 	System.out.format("%s: %s %s\n", event.kind().name(), child, new Date(child.toFile().lastModified()).toString() );
			}
        };
        watchdir.processEvents();
    }
}
