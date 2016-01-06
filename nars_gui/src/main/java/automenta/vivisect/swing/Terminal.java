package automenta.vivisect.swing;

/**
 * Created by me on 3/2/15.
 */
public class Terminal {

	// final long updatePeriodMS = 25;
	// final int HISTORY_SIZE = 16;
	// private final Process p;
	// private final PrintStream o;
	// public final Deque<String> history = new ArrayDeque();
	// boolean reading = true;
	// String currentLine = "";
	//
	// public class View extends SwingText implements Runnable, KeyListener {
	//
	// boolean updateNecessary = true;
	//
	// public View() {
	// super();
	// setBackground(Color.BLACK);
	// setFont(Video.monofont);
	// addKeyListener(this);
	// }
	//
	// public void update() {
	// if (updateNecessary) {
	// updateNecessary = false;
	// SwingUtilities.invokeLater(this);
	// }
	// }
	//
	// @Override
	// public void run() {
	// updateNecessary = true;
	// setText("");
	// synchronized (history) {
	// for (String s : history) {
	// print(Color.GRAY, s);
	// print(Color.GRAY, "\n");
	// }
	// }
	//
	// print(Color.WHITE, currentLine);
	// print(Color.WHITE, "_");
	// }
	//
	// @Override
	// public void keyTyped(KeyEvent e) {
	//
	// }
	//
	// @Override
	// public void keyPressed(KeyEvent e) {
	//
	// }
	//
	// @Override
	// public void keyReleased(KeyEvent e) {
	// char c = e.getKeyChar();
	// out(c, true);
	// }
	// }
	//
	// public final View view = new View();
	//
	// public Terminal() throws IOException {
	//
	// //new NWindow("x", view).show(600,400);
	//
	// String s;
	//
	// String command = "/bin/bash --restricted";
	// p = Runtime.getRuntime().exec(command);
	//
	// o = new PrintStream(p.getOutputStream());
	// /*
	// BufferedReader stdInput = new BufferedReader(new
	// InputStreamReader(p.getInputStream()));
	//
	// BufferedReader stdError = new BufferedReader(new
	// InputStreamReader(p.getErrorStream()));*/
	//
	// o.println("ls /");
	// o.println("w");
	// o.println("free");
	// o.flush();
	//
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// read();
	// }
	// }).start();
	// //
	// // while (p.isAlive()) {
	// //
	// //
	// // if (Math.random() < 0.5) {
	// // o.print('x');
	// // System.out.print('x');
	// // o.flush();
	// // }
	// // else if (Math.random() < 0.1) {
	// // o.println('\n');
	// // System.out.print('\n');
	// // o.flush();
	// // }
	// //
	// // try {
	// // Thread.sleep(updatePeriodMS);
	// // } catch (InterruptedException e) { }
	// // }
	//
	// try {
	// Thread.sleep(5000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// o.println("ls /tmp");
	// o.flush();
	//
	// //p.waitFor();
	//
	// // // read the output from the command
	// // System.out.println("Here is the standard output of the command:\n");
	// // while ((s = stdInput.readLine()) != null) {
	// // System.out.println(s);
	// // }
	// //
	// // // read any errors from the attempted command
	// //
	// System.out.println("Here is the standard error of the command (if any):\n");
	// // while ((s = stdError.readLine()) != null) {
	// // System.out.println(s);
	// // }
	//
	//
	// }
	//
	// public void out(char c, boolean echo) {
	// if (echo)
	// in(c, -1);
	// o.print(c);
	// o.flush();
	// onInput(c);
	// }
	//
	// public void in(char c, int channel) {
	// if (c != '\n') {
	// currentLine = currentLine + c;
	// onOutput(c);
	// }
	// else {
	// synchronized (history) {
	// if (history.size() + 1 > HISTORY_SIZE) {
	// history.removeFirst();
	// }
	// history.add(currentLine);
	// }
	//
	// onInputLine(currentLine);
	//
	// currentLine = "";
	// }
	//
	// view.update();
	// }
	//
	// protected void onInputLine(String currentLine) {
	//
	// }
	//
	// protected void onInput(char c) {
	//
	// }
	//
	// protected void onOutput(char c) {
	//
	// }
	//
	// public String getCurrentLine() { return currentLine; }
	//
	//
	// public void read() {
	// while (reading && p.isAlive()) {
	//
	// int c;
	// try {
	// //if (p.getInputStream().available() > 0)
	// {
	// while ((c = p.getInputStream().read()) > -1) {
	// in(Character.valueOf((char) c), 0);
	// }
	// }
	// //if (p.getErrorStream().available() > 0) {
	// {
	// while ((c = p.getErrorStream().read()) > -1) {
	//
	// in(Character.valueOf((char) c), 1);
	// }
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	//
	// try {
	// Thread.sleep(updatePeriodMS);
	// } catch (InterruptedException e) { }
	//
	// }
	// }
	// public static void main(String[] args) throws InterruptedException,
	// IOException {
	//
	// new Terminal();
	//
	//
	// }

}
