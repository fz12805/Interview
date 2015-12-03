import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Exception;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Considering the size of file, the amount of time to transfer the file could be 
//more than 1 min, we employ MD5 to verify file integrity.

public class OutputFile {
	private String dir;
	private int port;
	
	public OutputFile(String dir, int port) {
		this.dir = dir;
		this.port = port;
	}
	
	public void loop(String ip, int port) {
		ArrayList<String> outputFiles = new ArrayList<String>();
		while (true) {
			Map<String, String> fileInfo = retrvFileThroughFTP(ip, port);
			File newFile = new File(fileInfo.get(dir + "\\fileName"));
			if (verifyFile(newFile, fileInfo.get("md5"))) {
				File file = new File(dir);
				if(!file.isDirectory()) {
					System.err.println(dir + " should be a directory!");
				}
				String[] files = file.list();
				ArrayList<String> updatedFiles = checkUpdate(outputFiles, files);
				outputFiles.addAll(updatedFiles);
				outputFile(updatedFiles, port);
			}
			try {
				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Map<String, String> retrvFileThroughFTP(String ip, int port) {
		ClientSocket cs = new ClientSocket(ip, port);
		String md5 = new String();
		String fileName = new String();
		try {
			cs.CreateConnection();
            DataInputStream inputStream = cs.getMessageStream();
            md5 = inputStream.readUTF();
            fileName = inputStream.readUTF();
            cs.shutDownConnection();
		} catch (Exception e) {
            e.printStackTrace();
        }
		HashMap<String, String> fileInfo = new HashMap<String, String>();
		fileInfo.put("md5", md5);
		fileInfo.put("fileName", fileName);
		return fileInfo;
	}
	
	private boolean verifyFile(File file, String MD5) {
		if (getMd5ByFile(file).equals(MD5)) {
			return true;
		}
		return false;
	}
	
	private ArrayList<String> checkUpdate(ArrayList<String> outputFiles, String[] files) {
		ArrayList<String> updatedFiles = new ArrayList<String>();
		for (String file : files) {
			if (!outputFiles.contains(file)) {
				updatedFiles.add(file);
			}
		}
		return updatedFiles;
	}
	
	private void outputFile(ArrayList<String> filesPath, int port) {
		try {
			ServerSocket ss = new ServerSocket(port);
			Socket s = ss.accept();
			for (String pathname : filesPath) {
				DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(pathname)));
				DataOutputStream ps = new DataOutputStream(s.getOutputStream());
				int bufferSize = 8196;
				byte[] buf = new byte[bufferSize];
				while(true) {
					int read = 0;
					if(fis != null) {
						read = fis.read(buf);
					}
					if(read == -1) {
						break;
					}
					ps.write(buf,0,read);
				}
				ps.flush();
				ps.close();
				fis.close();
				s.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getMd5ByFile(File file) {
		String value = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}
}
