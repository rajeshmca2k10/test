package org.tcp.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * @author Rajesh
 *
 */

public class Client {

	File dir;
	File[] filesInfo;
	long fileSize;
	String[] metaInfo;
	int fileCount;
	public static int DELIMITER = 3;

	public static void main(String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
		Client client = new Client();
		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		String value = "";
		socket = new Socket(host.getHostName(), 9876);
		boolean flag = true;
		Scanner scanner = new Scanner(System.in);
		while (flag) {
			System.out.println("To Exit press \"exit\" ");
			System.out.println("Enter the directory Value:");
			value = scanner.nextLine();
			if (value.equals("exit")) {
				client.disconnectServer(socket);
				socket.close();
				scanner.close();
				flag = false;
				break;
			}
			client.sendHandShakeMSG(value, socket);
			client.sendFiles(socket);
			client.getHandShakeMSG(socket);
			client.getResponseData(socket);
			client.reset(socket);
			Thread.sleep(1000);
		}
		System.out.println("Client Disconnected!!!");

	}

	private void disconnectServer(Socket socket) throws IOException {
		socket.getOutputStream().write("exit".getBytes());
		socket.getOutputStream().write(DELIMITER);
		socket.getOutputStream().flush();

	}
	
	private void reset(Socket socket) throws IOException {

		dir = null;
		filesInfo = null;
		fileSize = 0;
		metaInfo = null;
		fileCount = 0;

	}

	private void getResponseData(Socket socket) throws IOException {

		InputStream os = socket.getInputStream();
		for (int i = 1; i <= fileCount; i++) {
			String[] fileInfo = metaInfo[i].split("\t");
			int fileSize = Integer.parseInt(fileInfo[0].trim());
			String filePath = fileInfo[1];
			byte b[] = new byte[fileSize];
			os.read(b);
			int fileNameCounter = filePath.lastIndexOf(File.separator);
			String dirPath = filePath.split(":")[1].substring(0, fileNameCounter - 1);
			File directory = new File("clienttemp" + dirPath);
			String fileName = filePath.substring(fileNameCounter + 1);
			File newFile = new File(directory, fileName);
			directory.mkdirs();
			newFile.createNewFile();
			OutputStream fos = new FileOutputStream(newFile);
			fos.write(b);
			fos.flush();
		}
		System.out.println("getResponseData Completed");
	}
	
	private void sendFiles(Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		for (File file : filesInfo) {
			byte value[] = Files.readAllBytes(file.toPath());
			os.write(value);
			os.flush();
		}
		System.out.println("SendFiles Completed");

	}

	private void sendHandShakeMSG(String path, Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		dir = new File(path);
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
		System.out.println("sendHandShakeMSG Completed");

	}
    
	private void getHandShakeMSG(Socket socket) throws IOException {
		InputStream os = socket.getInputStream();
		int value = os.read();
		StringBuffer sb = new StringBuffer();
		while (value != 3) {
			sb.append((char) value);
			value = os.read();
		}
		System.out.println("===>" + sb);
		metaInfo = sb.toString().split("\n");
		fileCount = Integer.parseInt(metaInfo[0]);
		System.out.println("getHandShakeMSG Completed");

	}
}
