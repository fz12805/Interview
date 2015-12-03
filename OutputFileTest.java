
public class OutputFileTest {
	public static void main(String[] args) {
		String clientIP = args[0];
		int clientPort = Integer.parseInt(args[1]);
		int port = 9527;
		OutputFile of = new OutputFile(args[0], port);
		of.loop(clientIP, clientPort);
	}
}
