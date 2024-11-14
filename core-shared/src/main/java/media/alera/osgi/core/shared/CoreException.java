package media.alera.osgi.core.shared;

public class CoreException extends Exception {
 
  public CoreException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public CoreException(String msg) {
    super(msg);
  }

  public CoreException(Throwable cause) {
    super(cause);
  }  
}
