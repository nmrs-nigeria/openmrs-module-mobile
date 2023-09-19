package pbsprocessor.listerner;

public interface ProgressListener {
     public  void  onError(String errorMessage);
    public  void  onProgress(   int progress, int  size, String displayMessage, String errorMessage);
    public  void  stop();
    public  void onStart(int size);
    public  void  serverOkay( boolean isRunning);


}
