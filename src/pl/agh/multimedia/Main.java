package pl.agh.multimedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pl.edu.agh.kis.visca.ViscaResponseReader;
import pl.edu.agh.kis.visca.cmd.*;
import jssc.*;

public class Main {

	public static int check = 0;
	
	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
	}
	
	static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
 //           String response = "This is the response";
        	
        	if(check > 0) {
        	String out = executePostBody(t);
        	executeCommand(out);
        	}
        	check = check + 1;
        	
        	
        	byte[] response = htmlPage.getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
            
        }
    }
	
    private static String htmlPage = "<html><body>\n" +
            "<h3>Rozkazuj:</h3>\n" +
            "<form name=\"formExample\" method=\"post\">\n" +
            "    Polecenie: <br> <input type=\"text\" name=\"comm\" value=\"\"> <br>\n" +
            "    <input type=\"submit\" value=\"Rozkaz!\">\n" +
            "</form>\n" +
            "</body></html>";

	static String executePostBody(HttpExchange t) throws IOException {
	    InputStreamReader isr =  new InputStreamReader(t.getRequestBody(),"utf-8");
	    BufferedReader br = new BufferedReader(isr);
	
	
	    int polecenie;
	    StringBuilder buf = new StringBuilder(512);
	    while ((polecenie = br.read()) != -1) {
	        buf.append((char) polecenie);
	    }
	
//	    System.out.println("Buf: " + buf.toString());
	
	    String command = buf.toString().split("=")[1];
//	    String comm = buf.toString().split("=")[1];
//	    String comm2 = comm.toString().split("+")[1];
	    
//	    System.out.println("COM2: " + comm2);
	    
//	    for(String s : comm2) {
//	    	System.out.println("Napis: "+s+" ");
//	    }
	    
	    //System.out.println(command);
	    return command;
	}
	
	static void executeCommand(String str) {
//		System.out.println("Napisano: "+ str);
		
		String[] command = new String[1];
//		System.out.println(str.indexOf("+"));
		if(str.indexOf("+") != -1) {
//			System.out.println("Jest +: "+ str);
			command = str.split("\\+");
		}else {
			command[0] = str;
//			System.out.println("Nie ma: "+ str);
		}
		
//		for(String s : command) {
//			System.out.println("nap: " + s);
//		}
		
//		System.out.println("Command: " + command);		
		execute(command);
	}
	
	public static void execute(String[] args) {
//		for(String s : args) {
//			System.out.println("exe: " + s);
//		}
		// write your code here
	        String portNumber = "COM11";
	        SerialPort port = new SerialPort(portNumber);

	        try {
	            port.openPort();
	            port.setParams(9600, 8, 1, 0);
	        } catch (SerialPortException e) {
	            e.printStackTrace();
	        }

////	        while(true){
////	            System.out.println("Enter VISCA command: ");
////	            Scanner sc = new Scanner(System.in);
////	            String command = sc.nextLine();
	            String[] cmd = args;//command.split(" ");
	            String comm = cmd[0];
	            int val = (byte) 0;
	            int val2 = (byte) 0;
	            if (cmd.length >1){
	                val = Integer.parseInt(cmd[1]);
	            }
	            if (cmd.length >2){
	                val2 = Integer.parseInt(cmd[2]);
	            }
	            System.out.println("Doing: " + comm + " with argument: " + val);
	            byte[] ex;

	            try {
	                byte[] cmdData = {};
	                switch (cmd[0]) {
	                    case ("left"):
	                        cmdData = (new PanTiltLeftCmd()).createCommandData();
	                        cmdData[3] = (byte) val;
	                        break;
	                    case ("right"):
	                        cmdData = (new PanTiltRightCmd()).createCommandData();
	                        cmdData[3] = (byte) val;
	                        break;
	                    case ("up"):
	                        cmdData = (new PanTiltUpCmd()).createCommandData();
	                        cmdData[3] = (byte) val;
	                        break;
	                    case ("down"):
	                        cmdData = (new PanTiltDownCmd()).createCommandData();
	                        cmdData[3] = (byte) val;
	                        break;
	                    case ("home"):
	                        cmdData = (new PanTiltHomeCmd()).createCommandData();
	                        break;
	                    case ("zoomWide"):
	                        cmdData = (new ZoomWideStdCmd()).createCommandData();
	                        break;
	                    case ("zoomTele"):
	                        cmdData = (new ZoomTeleStdCmd()).createCommandData();
	                        break;
	                    default:
	                        System.out.println("Not recognized command");
	                }

	                ViscaCommand vCmd = new ViscaCommand();
	                vCmd.commandData = cmdData;
	                vCmd.sourceAdr = 0;
	                vCmd.destinationAdr = 1;
	                cmdData = vCmd.getCommandData();
	                System.out.println("@ " + byteArrayToString(cmdData));
	                try {
	                    port.writeBytes(cmdData);
	                } catch (SerialPortException e) {
	                    e.printStackTrace();
	                }

	                try {
	                    ex = ViscaResponseReader.readResponse(port);
	                    System.out.println("> " + byteArrayToString(ex));
	                } catch (ViscaResponseReader.TimeoutException var11) {
	                    System.out.println("! TIMEOUT exception");
	                }
	            }catch(SerialPortException e){
	                e.printStackTrace();
	            }


	            try {
	                Thread.sleep(1500);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
////	        } koniec petli while
	  }
	
	    private static String byteArrayToString(byte[] bytes) {
	        StringBuilder sb = new StringBuilder();
	        byte[] var5 = bytes;
	        int var4 = bytes.length;
	
	        for(int var3 = 0; var3 < var4; ++var3) {
	            byte b = var5[var3];
	            sb.append(String.format("%02X ", new Object[]{Byte.valueOf(b)}));
	        }
	
	        return sb.toString();
	    }
	
}
