package org.tcp.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;

/**
 * @author Rajesh
 *
 */
public class Server {
	private static ServerSocket server1;

	private static int port = 9876;
	public static int DELIMITER = 3;
	File[] filesInfo;
	File directory;
	String[] metaInfo;
	int fileCount;
	String message = "";

	public static void main(String args[]) throws IOException, ClassNotFoundException {

		server1 = new ServerSocket(port);

		Socket socket = server1.accept();
		Server server = new Server();
		while (true) {
			System.out.println("Waiting for client request");

			InputStream ois = socket.getInputStream();
			server.getHandShakeMSG(socket);

			if ("exit".equals(server.message)) {
				ois.close();
				socket.close();
				System.out.println("Socket server listing !!!");
				socket = server1.accept();
				ois = socket.getInputStream();
				server.getHandShakeMSG(socket);

			}
			server.getFiles(socket);
			server.processData();
			server.sendResponseData(socket);
			server.sendFiles(socket);
			server.reset(socket);

		}

	}

	/**
	 * @param socket 
	 * @throws IOException
	 */
	private void getHandShakeMSG(Socket socket) throws IOException {
		InputStream os = socket.getInputStream();
		int value = os.read();
		StringBuffer sb = new StringBuffer();
		while (value != 3) {
			sb.append((char) value);
			value = os.read();
		}
		if ("exit".equals(sb.toString())) {
			message = sb.toString();
			return;
		}
		metaInfo = sb.toString().split("\n");
		fileCount = Integer.parseInt(metaInfo[0]);
		System.out.println("Server getHandShakeMSG Completed");
	}

	/**
	 * @param socket
	 * @throws IOException
	 */
	private void reset(Socket socket) throws IOException {

		filesInfo = null;
		directory = null;
		metaInfo = null;
		fileCount = 0;

	}

	private void processData() throws IOException {
		File files[] = directory.listFiles();
		StringBuffer sb;
		for (File fObj : files) {
			sb = new StringBuffer();
			List<String> content = Files.readAllLines(fObj.toPath());
			for (int i = 0; i < content.size(); i++) {
				sb.append(content.get(i) + " VERIFIED " + "\n");
			}
			FileWriter fileWriter = new FileWriter(fObj);
			fileWriter.write(sb.toString());
			fileWriter.flush();
			fileWriter.close();
		}
		System.out.println("Server processData Completed");
	}

	private void sendResponseData(Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		File dir = new File(directory.getAbsolutePath());
		filesInfo = dir.listFiles();
		StringBuffer buffer = new StringBuffer();

		buffer.append(filesInfo.length + "\n");
		for (File file : filesInfo) {
			buffer.append(file.length() + "\t");
			buffer.append(file.getPath() + "\n");
		}

		os.write(buffer.toString().getBytes());
		os.write(DELIMITER);
		os.flush();
		System.out.println("Server sendResponseData Completed");

	}

	private void sendFiles(Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		for (File file : filesInfo) {
			byte value[] = Files.readAllBytes(file.toPath());
			os.write(value);
			os.flush();
		}
		System.out.println("Server sendFiles Completed");
	}

	private void getFiles(Socket socket) throws IOException {

		InputStream os = socket.getInputStream();

		for (int i = 1; i <= fileCount; i++) {
			String[] fileInfo = metaInfo[i].split("\t");
			int fileSize = Integer.parseInt(fileInfo[0].trim());
			String filePath = fileInfo[1];
			byte b[] = new byte[fileSize];
			os.read(b);
			int fileNameCounter = filePath.lastIndexOf(File.separator);
			String dirPath = filePath.split(":")[1].substring(0, fileNameCounter - 1);
			directory = new File("temp" + dirPath);
			String fileName = filePath.substring(fileNameCounter + 1);
			File newFile = new File(directory, fileName);
			directory.mkdirs();
			newFile.createNewFile();
			OutputStream fos = new FileOutputStream(newFile);
			fos.write(b);
			fos.flush();
		}
		System.out.println("Server getFiles Completed");

	}

}
