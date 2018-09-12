import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Client {

	private static Socket clientSocket;
	private static PrintWriter out;
	private static BufferedReader in;
	private static String ret = "";
	private static ButtonPanel panel;

	public static void main(String[] args) throws InterruptedException, IOException {
		startConnection("127.0.0.1", 12365);
		//startConnection("71.46.74.181", 12365);
		Scanner scan = new Scanner(System.in);
		while (true) {
			String inp = in.readLine();
			if (inp.equals("What is your name?")) {
				System.out.println(inp);
				out.println(scan.nextLine());
			}
			else if (inp.contains("Yes or no?") || inp.contains("Players: ")) {
				int titleLoc = inp.indexOf("Yes or no?") >= 0 ? inp.indexOf("Yes or no?") : inp.indexOf("Players: ");
				String title = inp.substring(0, titleLoc);
				String[] text = {"Yes", "No"};
				if (!inp.contains("Yes or no?"))
					text = inp.substring(titleLoc + "Players: ".length()).split(",");
				panel = new ButtonPanel(title, text);
				ret = "";
				while (ret.equals(""))
					Thread.sleep(1000);
				out.println(ret);
			}
			else if (inp.contains("You have died.") || inp.contains("Game over.")) {
				stopConnection();
				panel.dispatchEvent(new WindowEvent(panel, WindowEvent.WINDOW_CLOSING));
				scan.close();
				break;
			}
			else {
				System.out.println(inp);
			}
		}
	}

	public static void startConnection(String ip, int port) throws IOException {
		clientSocket = new Socket(ip, port);
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public static String sendMessage(String msg) throws IOException {
		out.println(msg);
		String resp = in.readLine();
		return resp;
	}

	public static void stopConnection() throws IOException {
		in.close();
		out.close();
		clientSocket.close();
	}

	public static class ButtonPanel extends JFrame implements ActionListener {

		public ButtonPanel(String t, String[] text) {
			super(t);
			setLayout(new FlowLayout());
			for (String s: text) {
				JButton button = new JButton(s);
				add(button);
				button.addActionListener(this);
			}
			setSize(1200, 1000);
			setVisible(true);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ret = ((JButton) e.getSource()).getText();
			this.setVisible(false);
		}
		
		public String getRet() {
			return ret;
		}
		
		public void resetRet() {
			ret = "";
		}

	}

}
