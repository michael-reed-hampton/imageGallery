package name.hampton.mike;

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
 * Watch a directory (or tree) for changes to files.
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
}
