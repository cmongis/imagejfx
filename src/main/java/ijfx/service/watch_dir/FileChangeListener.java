
package ijfx.service.watch_dir;

/**
 * Interface definition for a callback to be invoked when a file under
 * watch is changed.
 */
public interface FileChangeListener {

    /**
     * Called when the file is created.
     * @param filePath The file path.
     */
    default void onFileCreate(String filePath) {
    }

    /**
     * Called when the file is modified.
     * @param filePath The file path.
     */
    default void onFileModify(String filePath) {
    }

    /**
     * Called when the file is deleted.
     * @param filePath The file path.
     */
    default void onFileDelete(String filePath) {
    }
    
}
