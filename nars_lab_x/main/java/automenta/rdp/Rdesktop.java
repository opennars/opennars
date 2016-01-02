/* Rdesktop.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Main class, launches session
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 * 
 */

package automenta.rdp;

import automenta.rdp.keymapping.KeyCode_FileBased;
import automenta.rdp.rdp.KeyCode_FileBased_Localised;
import automenta.rdp.rdp.RdesktopFrame_Localised;
import automenta.rdp.rdp5.Rdp5;
import automenta.rdp.rdp5.VChannels;
import automenta.rdp.rdp5.cliprdr.ClipChannel;
import automenta.rdp.rdp5.disk.DiskChannel;
import automenta.rdp.rdp5.snd.SoundChannel;
import automenta.rdp.tools.SendEvent;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Rdesktop {

	/**
	 * Translate a disconnect code into a textual description of the reason for
	 * the disconnect
	 * 
	 * @param reason
	 *            Integer disconnect code received from server
	 * @return Text description of the reason for disconnection
	 */
	static String textDisconnectReason(int reason) {
		String text;

		switch (reason) {
		case exDiscReasonNoInfo:
			text = "No information available";
			break;

		case exDiscReasonAPIInitiatedDisconnect:
			text = "Server initiated disconnect";
			break;

		case exDiscReasonAPIInitiatedLogoff:
			text = "Server initiated logoff";
			break;

		case exDiscReasonServerIdleTimeout:
			text = "Server idle timeout reached";
			break;

		case exDiscReasonServerLogonTimeout:
			text = "Server logon timeout reached";
			break;

		case exDiscReasonReplacedByOtherConnection:
			text = "Another user connected to the session";
			break;

		case exDiscReasonOutOfMemory:
			text = "The server is out of memory";
			break;

		case exDiscReasonServerDeniedConnection:
			text = "The server denied the connection";
			break;

		case exDiscReasonServerDeniedConnectionFips:
			text = "The server denied the connection for security reason";
			break;

		case exDiscReasonLicenseInternal:
			text = "Internal licensing error";
			break;

		case exDiscReasonLicenseNoLicenseServer:
			text = "No license server available";
			break;

		case exDiscReasonLicenseNoLicense:
			text = "No valid license available";
			break;

		case exDiscReasonLicenseErrClientMsg:
			text = "Invalid licensing message";
			break;

		case exDiscReasonLicenseHwidDoesntMatchLicense:
			text = "Hardware id doesn't match software license";
			break;

		case exDiscReasonLicenseErrClientLicense:
			text = "Client license error";
			break;

		case exDiscReasonLicenseCantFinishProtocol:
			text = "Network error during licensing protocol";
			break;

		case exDiscReasonLicenseClientEndedProtocol:
			text = "Licensing protocol was not completed";
			break;

		case exDiscReasonLicenseErrClientEncryption:
			text = "Incorrect client license enryption";
			break;

		case exDiscReasonLicenseCantUpgradeLicense:
			text = "Can't upgrade license";
			break;

		case exDiscReasonLicenseNoRemoteConnections:
			text = "The server is not licensed to accept remote connections";
			break;

		default:
			if (reason > 0x1000 && reason < 0x7fff) {
				text = "Internal protocol error: 0x" + Integer.toHexString(reason);
			} else {
				text = "Unknown reason";
			}
		}
		return text;
	}

	/* RDP5 disconnect PDU */
	public static final int exDiscReasonNoInfo = 0x0000;

	public static final int exDiscReasonAPIInitiatedDisconnect = 0x0001;

	public static final int exDiscReasonAPIInitiatedLogoff = 0x0002;

	public static final int exDiscReasonServerIdleTimeout = 0x0003;

	public static final int exDiscReasonServerLogonTimeout = 0x0004;

	public static final int exDiscReasonReplacedByOtherConnection = 0x0005;

	public static final int exDiscReasonOutOfMemory = 0x0006;

	public static final int exDiscReasonServerDeniedConnection = 0x0007;

	public static final int exDiscReasonServerDeniedConnectionFips = 0x0008;

	public static final int exDiscReasonLicenseInternal = 0x0100;

	public static final int exDiscReasonLicenseNoLicenseServer = 0x0101;

	public static final int exDiscReasonLicenseNoLicense = 0x0102;

	public static final int exDiscReasonLicenseErrClientMsg = 0x0103;

	public static final int exDiscReasonLicenseHwidDoesntMatchLicense = 0x0104;

	public static final int exDiscReasonLicenseErrClientLicense = 0x0105;

	public static final int exDiscReasonLicenseCantFinishProtocol = 0x0106;

	public static final int exDiscReasonLicenseClientEndedProtocol = 0x0107;

	public static final int exDiscReasonLicenseErrClientEncryption = 0x0108;

	public static final int exDiscReasonLicenseCantUpgradeLicense = 0x0109;

	public static final int exDiscReasonLicenseNoRemoteConnections = 0x010a;

	static Logger logger = Logger.getLogger("net.propero.rdp");

	static boolean keep_running;

	static boolean loggedon;
	
	static int gReconnectLogonid = 0;

	public static boolean readytosend;

	static boolean showTools;

	static final String keyMapPath = "keymaps/";

	static String mapFile = "en-us";

	static String keyMapLocation = "";

	static SendEvent toolFrame = null;

	/**
	 * Outputs version and usage information via System.err
	 * 
	 */
	public static void usage() {
		System.err.println("properJavaRDP version " + Version.version);
		System.err
				.println("Usage: java net.propero.rdp.Rdesktop [options] server[:port]");
		System.err
				.println("	-b 							bandwidth saving (good for 56k modem, but higher latency");
		System.err.println("	-c DIR						working directory");
		System.err.println("	-d DOMAIN					logon domain");
		System.err
				.println("	-f[l]						full-screen mode [with Linux KDE optimization]");
		System.err.println("	-g WxH						desktop geometry");
		System.err
				.println("	-m MAPFILE					keyboard mapping file for terminal server");
		System.err
				.println("	-l LEVEL					logging level {DEBUG, INFO, WARN, ERROR, FATAL}");
		System.err.println("	-n HOSTNAME					client hostname");
		System.err.println("	-p PASSWORD					password");
		System.err.println("	-s SHELL					shell");
		System.err.println("	-t NUM						RDP port (default 3389)");
		System.err.println("	-T TITLE					window title");
		System.err.println("	-u USERNAME					user name");
		System.err.println("	-o BPP						bits-per-pixel for display");
		System.err
				.println("    -r path                     path to load licence from (requests and saves licence from server if not found)");
		System.err
				.println("    --save_licence              request and save licence from server");
		System.err
				.println("    --load_licence              load licence from file");
		System.err
				.println("    --console                   connect to console");
		System.err
				.println("	--debug_key 				show scancodes sent for each keypress etc");
		System.err.println("	--debug_hex 				show bytes sent and received");
		System.err.println("	--no_remap_hash 			disable hash remapping");
		System.err.println("	--quiet_alt 				enable quiet alt fix");
		System.err
				.println("	--no_encryption				disable encryption from client to server");
		System.err.println("	--use_rdp4					use RDP version 4");
		// System.err.println(" --enable_menu enable menu bar");
		System.err
				.println("	--log4j_config=FILE			use FILE for log4j configuration");
		System.err
				.println("Example: java net.propero.rdp.Rdesktop -g 800x600 -l WARN m52.propero.int");
		Rdesktop.exit(0, null, null, true);
	}

	/**
	 * 
	 * @param args
	 * @throws OrderException
	 * @throws RdesktopException
	 */
	public static void main(String[] args) throws RdesktopException {

		// Ensure that static variables are properly initialised
		keep_running = true;
		loggedon = false;
		readytosend = false;
		showTools = false;
		mapFile = "en-us";
		keyMapLocation = "";
		toolFrame = null;

		BasicConfigurator.configure();
		logger.setLevel(Level.WARN);
		Logger.getRootLogger().setLevel(Level.INFO);

		// Attempt to run a native RDP Client

		//RDPClientChooser Chooser = new RDPClientChooser();

		/*
		if (Chooser.RunNativeRDPClient(args)) {
			if (!Common.underApplet)
				System.exit(0);
		}
		*/

		// Failed to run native client, drop back to Java client instead.

		// parse arguments

		int logonflags = Rdp.RDP_LOGON_NORMAL;

		boolean fKdeHack = false;
		int c;
		String arg;
		StringBuffer sb = new StringBuffer();
		LongOpt[] alo = new LongOpt[15];
		alo[0] = new LongOpt("debug_key", LongOpt.NO_ARGUMENT, null, 0);
		alo[1] = new LongOpt("debug_hex", LongOpt.NO_ARGUMENT, null, 0);
		alo[2] = new LongOpt("no_paste_hack", LongOpt.NO_ARGUMENT, null, 0);
		alo[3] = new LongOpt("log4j_config", LongOpt.REQUIRED_ARGUMENT, sb, 0);
		alo[4] = new LongOpt("packet_tools", LongOpt.NO_ARGUMENT, null, 0);
		alo[5] = new LongOpt("quiet_alt", LongOpt.NO_ARGUMENT, sb, 0);
		alo[6] = new LongOpt("no_remap_hash", LongOpt.NO_ARGUMENT, null, 0);
		alo[7] = new LongOpt("no_encryption", LongOpt.NO_ARGUMENT, null, 0);
		alo[8] = new LongOpt("use_rdp4", LongOpt.NO_ARGUMENT, null, 0);
		alo[9] = new LongOpt("use_ssl", LongOpt.NO_ARGUMENT, null, 0);
		alo[10] = new LongOpt("enable_menu", LongOpt.NO_ARGUMENT, null, 0);
		alo[11] = new LongOpt("console", LongOpt.NO_ARGUMENT, null, 0);
		alo[12] = new LongOpt("load_licence", LongOpt.NO_ARGUMENT, null, 0);
		alo[13] = new LongOpt("save_licence", LongOpt.NO_ARGUMENT, null, 0);
		alo[14] = new LongOpt("persistent_caching", LongOpt.NO_ARGUMENT, null,
				0);

		String progname = "properJavaRDP";

		Getopt g = new Getopt("properJavaRDP", args,
				"bc:d:f::g:k:l:m:n:p:s:t:T:u:o:r:", alo);

		ClipChannel clipChannel = new ClipChannel();
		SoundChannel soundChannel = new SoundChannel();
		
		DiskChannel diskChannel = new DiskChannel();
		//diskChannel.addDiskDevice("linux", "/home/blee/temp/rdptest");
		//diskChannel.addDiskDevice("tmp", "/dev/shm");
		
//		RdpdrChannel diskChannel = new RdpdrChannel();
//		diskChannel.deviceRegister(new DiskRdpdrDevice("linux", "/home/blee/temp/rdptest"));
		
		while ((c = g.getopt()) != -1) {
			switch (c) {

			case 0:
				switch (g.getLongind()) {
				case 0:
					Options.debug_keyboard = true;
					break;
				case 1:
					Options.debug_hexdump = true;
					break;
				case 2:
					break;
				case 3:
					arg = g.getOptarg();
					PropertyConfigurator.configure(arg);
					logger.info("Log4j using config file " + arg);
					break;
				case 4:
					showTools = true;
					break;
				case 5:
					Options.altkey_quiet = true;
					break;
				case 6:
					Options.remap_hash = false;
					break;
				case 7:
					Options.packet_encryption = false;
					break;
				case 8:
					Options.use_rdp5 = false;
					// Options.server_bpp = 8;
					Options.set_bpp(8);
					break;
				case 9:
					Options.use_ssl = true;
					break;
				case 10:
					Options.enable_menu = true;
					break;
				case 11:
					Options.console_session = true;
					break;
				case 12:
					Options.load_licence = true;
					break;
				case 13:
					Options.save_licence = true;
					break;
				case 14:
					Options.persistent_bitmap_caching = true;
					break;
				default:
					usage();
				}
				break;

			case 'o':
				Options.set_bpp(Integer.parseInt(g.getOptarg()));
				break;
			case 'b':
				Options.low_latency = false;
				break;
			case 'm':
				mapFile = g.getOptarg();
				break;
			case 'c':
				Options.directory = g.getOptarg();
				break;
			case 'd':
				Options.domain = g.getOptarg();
				break;
			case 'f':
				Dimension screen_size = Toolkit.getDefaultToolkit()
						.getScreenSize();
				// ensure width a multiple of 4
				Options.width = screen_size.width & ~3;
				Options.height = screen_size.height;
				Options.fullscreen = true;
				arg = g.getOptarg();
				if (arg != null) {
					if (arg.charAt(0) == 'l')
						fKdeHack = true;
					else {
						System.err.println(progname
								+ ": Invalid fullscreen option '" + arg + '\'');
						usage();
					}
				}
				break;
			case 'g':
				arg = g.getOptarg();
				int cut = arg.indexOf('x', 0);
				if (cut == -1) {
					System.err.println(progname + ": Invalid geometry: " + arg);
					usage();
				}
				Options.width = Integer.parseInt(arg.substring(0, cut)) & ~3;
				Options.height = Integer.parseInt(arg.substring(cut + 1));
				break;
			case 'k':
				arg = g.getOptarg();
				// Options.keylayout = KeyLayout.strToCode(arg);
				if (Options.keylayout == -1) {
					System.err.println(progname + ": Invalid key layout: "
							+ arg);
					usage();
				}
				break;
			case 'l':
				arg = g.getOptarg();
				switch (arg.charAt(0)) {
				case 'd':
				case 'D':
					logger.setLevel(Level.DEBUG);
					break;
				case 'i':
				case 'I':
					logger.setLevel(Level.INFO);
					break;
				case 'w':
				case 'W':
					logger.setLevel(Level.WARN);
					break;
				case 'e':
				case 'E':
					logger.setLevel(Level.ERROR);
					break;
				case 'f':
				case 'F':
					logger.setLevel(Level.FATAL);
					break;
				default:
					System.err.println(progname + ": Invalid debug level: "
							+ arg.charAt(0));
					usage();
				}
				break;
			case 'n':
				Options.hostname = g.getOptarg();
				break;
			case 'p':
				Options.password = g.getOptarg();
				logonflags |= Rdp.RDP_LOGON_AUTO;
				break;
			case 's':
				Options.command = g.getOptarg();
				break;
			case 'u':
				Options.username = g.getOptarg();
				break;
			case 't':
				arg = g.getOptarg();
				try {
					Options.port = Integer.parseInt(arg);
				} catch (NumberFormatException nex) {
					System.err.println(progname + ": Invalid port number: "
							+ arg);
					usage();
				}
				break;
			case 'T':
				Options.windowTitle = g.getOptarg().replace('_', ' ');
				break;
			case 'r':
				Options.licence_path = g.getOptarg();
				break;

			case '?':
			default:
				usage();
				break;

			}
		}

		if (fKdeHack) {
			Options.height -= 46;
		}

		String server = null;

		if (g.getOptind() < args.length) {
			int colonat = args[args.length - 1].indexOf(':', 0);
			if (colonat == -1) {
				server = args[args.length - 1];
			} else {
				server = args[args.length - 1].substring(0, colonat);
				Options.port = Integer.parseInt(args[args.length - 1]
						.substring(colonat + 1));
			}
		} else {
			System.err.println(progname + ": A server name is required!");
			usage();
		}

		VChannels channels = new VChannels();

		// Initialise all RDP5 channels
		if (Options.use_rdp5) {
			// TODO: implement all relevant channels
			if (Options.map_clipboard) {
			    channels.register(clipChannel);
			}
			channels.register(soundChannel);
			channels.register(diskChannel);
		}

		// Now do the startup...

		logger.info("properJavaRDP version " + Version.version);

		if (args.length == 0)
			usage();


		logger.info("Java version is " + System.getProperty("java.specification.version"));

		String os = System.getProperty("os.name");
		String osvers = System.getProperty("os.version");

		if (os.equals("Windows 2000") || os.equals("Windows XP"))
			Options.built_in_licence = true;

		logger.info("Operating System is " + os + " version " + osvers);

		if (os.startsWith("Linux"))
			Constants.OS = Constants.LINUX;
		else if (os.startsWith("Windows"))
			Constants.OS = Constants.WINDOWS;
		else if (os.startsWith("Mac"))
			Constants.OS = Constants.MAC;

		if (Constants.OS == Constants.MAC)
			Options.caps_sends_up_and_down = false;

		Rdp5 RdpLayer = null;
		Common.rdp = RdpLayer;
		RdesktopFrame window = new RdesktopFrame_Localised();
		window.setClip(clipChannel);

		// Configure a keyboard layout
		KeyCode_FileBased keyMap = null;
		try {
			// logger.info("looking for: " + "/" + keyMapPath + mapFile);
			InputStream istr = Rdesktop.class.getResourceAsStream('/'
					+ keyMapPath + mapFile);
			// logger.info("istr = " + istr);
			if (istr == null) {
				logger.debug("Loading keymap from filename");
				keyMap = new KeyCode_FileBased_Localised(keyMapPath + mapFile);
			} else {
				logger.debug("Loading keymap from InputStream");
				keyMap = new KeyCode_FileBased_Localised(istr);
			}
			if (istr != null)
				istr.close();
			Options.keylayout = keyMap.getMapCode();
		} catch (Exception kmEx) {
			String[] msg = { (kmEx.getClass() + ": " + kmEx.getMessage()) };
			window.showErrorDialog(msg);
			kmEx.printStackTrace();
			Rdesktop.exit(0, null, null, true);
		}

		logger.debug("Registering keyboard...");
		if (keyMap != null)
			window.registerKeyboard(keyMap);

		boolean[] deactivated = new boolean[1];
		int[] ext_disc_reason = new int[1];

		logger.debug("keep_running = " + keep_running);
		while (keep_running) {
			logger.debug("Initialising RDP layer...");
			RdpLayer = new Rdp5(channels);
			Common.rdp = RdpLayer;
			logger.debug("Registering drawing surface...");
			RdpLayer.registerDrawingSurface(window);
			logger.debug("Registering comms layer...");
			window.registerCommLayer(RdpLayer);
			loggedon = false;
			readytosend = false;
			logger.info("Connecting to " + server + ':' + Options.port
			            + " ...");

			if (server.equalsIgnoreCase("localhost"))
				server = "127.0.0.1";

			if (RdpLayer != null) {
				// Attempt to connect to server on port Options.port
				try {
					RdpLayer.connect(Options.username, InetAddress
							.getByName(server), logonflags, Options.domain,
							Options.password, Options.command,
							Options.directory);

					// Remove to get rid of sendEvent tool
					if (showTools) {
						toolFrame = new SendEvent(RdpLayer);
						toolFrame.show();
					}
					// End

					if (keep_running) {

						/*
						 * By setting encryption to False here, we have an
						 * encrypted login packet but unencrypted transfer of
						 * other packets
						 */
						if (!Options.packet_encryption)
							Options.encryption = false;

						logger.info("Connection successful");
						// now show window after licence negotiation
						RdpLayer.mainLoop(deactivated, ext_disc_reason);

						if (deactivated[0]) {
							/* clean disconnect */
							Rdesktop.exit(0, RdpLayer, window, true);
							// return 0;
						} else {
							if (ext_disc_reason[0] == exDiscReasonAPIInitiatedDisconnect
									|| ext_disc_reason[0] == exDiscReasonAPIInitiatedLogoff) {
								/*
								 * not so clean disconnect, but nothing to worry
								 * about
								 */
								Rdesktop.exit(0, RdpLayer, window, true);
								// return 0;
							}

							if (ext_disc_reason[0] >= 2) {
								String reason = textDisconnectReason(ext_disc_reason[0]);
								String msg[] = { "Connection terminated",
										reason };
								window.showErrorDialog(msg);
								logger.warn("Connection terminated: " + reason);
								Rdesktop.exit(0, RdpLayer, window, true);
							}

						}

						keep_running = false; // exited main loop
						if (!readytosend) {
							// maybe the licence server was having a comms
							// problem, retry?
							String msg1 = "The terminal server disconnected before licence negotiation completed.";
							String msg2 = "Possible cause: terminal server could not issue a licence.";
							String[] msg = { msg1, msg2 };
							logger.warn(msg1);
							logger.warn(msg2);
							window.showErrorDialog(msg);
						}
					} // closing bracket to if(running)

					// Remove to get rid of tool window
					if (showTools)
						toolFrame.dispose();
					// End

				} catch (ConnectionException e) {
					String msg[] = { "Connection Exception", e.getMessage() };
					window.showErrorDialog(msg);
					Rdesktop.exit(0, RdpLayer, window, true);
				} catch (UnknownHostException e) {
					error(e, RdpLayer, window, true);
				} catch (SocketException s) {
					if (RdpLayer.isConnected()) {
						logger.fatal(s.getClass().getName() + ' '
								+ s.getMessage());
						s.printStackTrace();
						error(s, RdpLayer, window, true);
						Rdesktop.exit(0, RdpLayer, window, true);
					}
				} catch (RdesktopException e) {
					String msg1 = e.getClass().getName();
					String msg2 = e.getMessage();
					logger.fatal(msg1 + ": " + msg2);

					e.printStackTrace(System.err);

					if (!readytosend) {
						// maybe the licence server was having a comms
						// problem, retry?
						String msg[] = {
								"The terminal server reset connection before licence negotiation completed.",
								"Possible cause: terminal server could not connect to licence server.",
								"Retry?" };
						boolean retry = window.showYesNoErrorDialog(msg);
						if (!retry) {
							logger.info("Selected not to retry.");
							Rdesktop.exit(0, RdpLayer, window, true);
						} else {
							if (RdpLayer != null && RdpLayer.isConnected()) {
								logger.info("Disconnecting ...");
								RdpLayer.disconnect();
								logger.info("Disconnected");
							}
							logger.info("Retrying connection...");
							keep_running = true; // retry
							continue;
						}
					} else {
						String msg[] = { e.getMessage() };
						window.showErrorDialog(msg);
						Rdesktop.exit(0, RdpLayer, window, true);
					}
				} catch (Exception e) {
					logger.warn(e.getClass().getName() + ' ' + e.getMessage());
					e.printStackTrace();
					error(e, RdpLayer, window, true);
				}
			} else { // closing bracket to if(!rdp==null)
				logger
						.fatal("The communications layer could not be initiated!");
			}
		}
		Rdesktop.exit(0, RdpLayer, window, true);
	}

	/**
	 * Disconnects from the server connected to through rdp and destroys the
	 * RdesktopFrame window.
	 * <p>
	 * Exits the application iff sysexit == true, providing return value n to
	 * the operating system.
	 * 
	 * @param n
	 * @param rdp
	 * @param window
	 * @param sysexit
	 */
	public static void exit(int n, Rdp rdp, RdesktopFrame window,
			boolean sysexit) {
		keep_running = false;

		// Remove to get rid of tool window
		if ((showTools) && (toolFrame != null))
			toolFrame.dispose();
		// End

		if (rdp != null && rdp.isConnected()) {
			logger.info("Disconnecting ...");
			rdp.disconnect();
			logger.info("Disconnected");
		}
		if (window != null) {
			window.setVisible(false);
			window.dispose();
		}

		System.gc();

		if (sysexit && Constants.SystemExit) {
			if (!Common.underApplet)
				System.exit(n);
		}
	}

	/**
	 * Displays an error dialog via the RdesktopFrame window containing the
	 * customised message emsg, and reports this through the logging system.
	 * <p>
	 * The application then exits iff sysexit == true
	 * 
	 * @param emsg
	 * @param RdpLayer
	 * @param window
	 * @param sysexit
	 */
	public static void customError(String emsg, Rdp RdpLayer,
			RdesktopFrame window, boolean sysexit) {
		logger.fatal(emsg);
		String[] msg = { emsg };
		window.showErrorDialog(msg);
		Rdesktop.exit(0, RdpLayer, window, true);
	}

	/**
	 * Displays details of the Exception e in an error dialog via the
	 * RdesktopFrame window and reports this through the logger, then prints a
	 * stack trace.
	 * <p>
	 * The application then exits iff sysexit == true
	 * 
	 * @param e
	 * @param RdpLayer
	 * @param window
	 * @param sysexit
	 */
	public static void error(Exception e, Rdp RdpLayer, RdesktopFrame window,
			boolean sysexit) {
		try {

			String msg1 = e.getClass().getName();
			String msg2 = e.getMessage();

			logger.fatal(msg1 + ": " + msg2);

			String[] msg = { msg1, msg2 };
			window.showErrorDialog(msg);

			// e.printStackTrace(System.err);
		} catch (Exception ex) {
			logger.warn("Exception in Rdesktop.error: "
					+ ex.getClass().getName() + ": " + ex.getMessage());
		}

		Rdesktop.exit(0, RdpLayer, window, sysexit);
	}
}
