
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class socket_server{
	public static final int Port = 2222;
	public static void main(String[] args) {
		ServerSocket serversocket = null;
		ExecutorService threadExecutor = Executors.newCachedThreadPool();
		try{
			serversocket = new ServerSocket(Port);
			System.out.println("Server listening requests..."+serversocket.getLocalPort());
			
			while(true){
				Socket socket = serversocket.accept();
				threadExecutor.execute( new RequestThread( socket ) );
			}
		}
		catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( threadExecutor != null )
                threadExecutor.shutdown();
            if ( serversocket != null )
                try
                {
                    serversocket.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
        }
	}

	static class RequestThread implements Runnable
    {
        private Socket clientSocket;
        
        public RequestThread( Socket clientSocket )
        {
            this.clientSocket = clientSocket;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run()
        {
            System.out.printf("��%s�s�u�i��!\n", clientSocket.getRemoteSocketAddress() );
            DataInputStream input = null;
            DataOutputStream output = null;
            
            try
            {
                input = new DataInputStream( this.clientSocket.getInputStream() );
                output = new DataOutputStream( this.clientSocket.getOutputStream() );
                //�ثe�Oserver�|���_��ť  ����ݭn���n��while����
                while ( true )
                {   
                	System.out.println("client say : " +input.readUTF() );//���T��
                	//================================================================================
                	//���ε��G
                	//========================================================================
                    output.writeUTF( String.format("Hi, %s!\n", clientSocket.getRemoteSocketAddress() ) );//�g�T��
                    output.flush();
                    //break;
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            finally 
            {
                try
                {
                    if ( input != null )
                        input.close();
                    if ( output != null )
                        output.close();
                    if ( this.clientSocket != null && !this.clientSocket.isClosed() )
                        this.clientSocket.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}

