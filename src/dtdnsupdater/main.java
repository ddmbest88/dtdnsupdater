package dtdnsupdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class main extends JFrame implements ActionListener {
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private JFrame frame;
	private JFrame frame2;
	private JMenu menu;
	private JMenuItem config;
	private JMenuBar bar;
	private JPanel contentPane;
	private JPanel contentPane2;
	JLabel ipaddress = new JLabel("");
	JLabel lastipaddress = new JLabel("");
	private String newip;
	JButton btnRinnovaDns = new JButton("Rinnova DNS");
	private JLabel updatestatus= new JLabel("<html><font color=\"#ff5733\">attesa...</font></html>");
	private String status="attesa...";
	String user = "", pass = "", id = "", lastIP = "",version="1.0";
	int minutes = 30;
	boolean exit;
	final File configFile = new File(System.getProperty("user.home"), ".dnsmadeeasy/config.txt");
	private JTextField userlbl;
	private JPasswordField passwordField;
	private JButton btnSalvaImpostazioni;
	private JButton btnRicontrollaIP;
	private boolean update = false;
	private String error="";
	private String checkip="";
	
	
	 public static void main (String[] args) throws Exception {
		
		try {
			File dir = new File(System.getProperty("user.home"), ".dnsmadeeasy");
			dir.mkdirs();
			FileOutputStream logFile = new FileOutputStream(new File(dir, "dnsmadeeasy.log"));
			System.setOut(new PrintStream(new MultiplexOutputStream(System.out, logFile), true));
			System.setErr(new PrintStream(new MultiplexOutputStream(System.err, logFile), true));
		} catch (Throwable ex) {
			log("Unable to write log file.", ex);
		}

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException (Thread thread, Throwable ex) {
				log("Uncaught exception, exiting.", ex);
				System.exit(0);
			}
		});

		new main();
	}
	
	

	public main() throws IOException {
		initialize();
		log("Started.", null);
		loadConfig();
		new Timer("Timer").schedule(new TimerTask() {
			public void run () {
				try {
					loadConfig();
					lastipaddress.setText("<html><font color=\"#ff5733\">"+lastIP+"</font></html>");
					update(id, pass);	
				} catch (IOException ex) {
					log("Error updating.", ex);
				}
				if (exit) System.exit(0);
			}
		}, 0, minutes * 60 * 1000);
	}
	
	
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 230);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("DtDns Updater "+version);
		frame.setResizable(false);
		newip=getIP();
		config= new JMenuItem();
		config.setText("Imposta configurazione...");
		config.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	startconfigframe();
         }
		});
		menu=new JMenu();
		menu.add(config);
		menu.setText("Impostazioni...");
		bar= new JMenuBar();
		bar.add(menu);
		frame.setJMenuBar(bar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		contentPane.setLayout(null);
		JLabel lblIlTuoIndirizzo = new JLabel("Il Tuo Indirizzo IP:");
		lblIlTuoIndirizzo.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblIlTuoIndirizzo.setBounds(26, 35, 117, 25);
		contentPane.add(lblIlTuoIndirizzo);
		JLabel lblIlTuoIndirizzoattuale = new JLabel("Ultimo Indirizzo IP:");
		lblIlTuoIndirizzoattuale.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblIlTuoIndirizzoattuale.setBounds(26, 60, 130, 25);
		contentPane.add(lblIlTuoIndirizzoattuale);
		ipaddress.setFont(new Font("Tahoma", Font.BOLD, 12));
		ipaddress.setBounds(138, 40, 210, 14);
		
		lastipaddress.setFont(new Font("Tahoma", Font.BOLD, 12));
		lastipaddress.setBounds(142, 65, 210, 14);
		contentPane.add(lastipaddress);
		
		btnRinnovaDns.setBounds(26, 146, 117, 23);
		btnRinnovaDns.addActionListener(this);
		contentPane.add(btnRinnovaDns);
		btnRicontrollaIP= new JButton();
		btnRicontrollaIP.setBounds(146, 146, 117, 23);
		btnRicontrollaIP.setText("Ricontrolla IP");
		btnRicontrollaIP.addActionListener(this);
		contentPane.add(btnRicontrollaIP);
		contentPane.add(ipaddress);
		JLabel lblStato = new JLabel("Stato:");
		lblStato.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblStato.setBounds(26, 108, 46, 14);
		contentPane.add(lblStato);
		updatestatus.setFont(new Font("Tahoma", Font.BOLD, 12));
		updatestatus.setBounds(67, 108, 350, 14);
		updatestatus.setText(status);
		contentPane.add(updatestatus);
		ImageIcon image;
		JLabel lblimage;
		 image = new ImageIcon(getClass().getResource("res/dns.png"));
		 //image = new ImageIcon("/src/rsc/dns.JPEG");
		 lblimage = new JLabel("", image, JLabel.CENTER);
		 lblimage.setBounds(280, 15, 90, 90);
		 frame.add(lblimage, BorderLayout.CENTER);
		
		frame.setVisible(true);
		ipaddress.setText("<html><font color=\"green\">"+newip+"</font></html>");
	}
	private void startconfigframe(){
		frame2 = new JFrame();
		frame2.setBounds(100, 100, 450, 230);
		frame2.setTitle("DtDns Updater "+version);
		frame2.setVisible(true);
		frame2.setResizable(false);
		contentPane2 = new JPanel();
		frame2.setContentPane(contentPane2);
		contentPane2.setLayout(null);
		JLabel lblUsernameid = new JLabel("Username(ID):");
		lblUsernameid.setBounds(29, 31, 94, 14);
		lblUsernameid.setFont(new Font("Tahoma", Font.BOLD, 12));
		contentPane2.add(lblUsernameid);
		
		userlbl = new JTextField();
		userlbl.setBounds(29, 56, 372, 20);
		contentPane2.add(userlbl);
		userlbl.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(29, 87, 94, 14);
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 12));
		contentPane2.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(29, 112, 193, 20);
		contentPane2.add(passwordField);
		
		btnSalvaImpostazioni = new JButton("Salva impostazioni");
		btnSalvaImpostazioni.setBounds(29, 162, 144, 33);
		btnSalvaImpostazioni.addActionListener(new ActionListener() {
	        @SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
	        	user=userlbl.getText().toString();
	        	id=userlbl.getText().toString();
	        	char[] input =passwordField.getPassword();
	        	pass="";
	        	for(int i=0;i<input.length;i++){
	        		pass=pass+input[i];
	        	}
	        	//pass=passwordField.getPassword();
	        	try{
	        		saveConfig();
	        		JOptionPane.showMessageDialog(null, "Salvataggio Avvenuto con successo", "InfoBox: " + "Successo!", JOptionPane.INFORMATION_MESSAGE);
	        	    frame2.dispose();
	        	}catch (Exception ex){
	        		JOptionPane.showMessageDialog(null, "Non riesco a salvare le configurazioni", "InfoBox: " + "Fallimento!", JOptionPane.INFORMATION_MESSAGE);
	        	    
	        		log("Non riesco a salvare le configurazioni.", ex);
	        	}
	         }
			});
		contentPane2.add(btnSalvaImpostazioni);
	}
	

	void update (String id, String pass) throws IOException {
		log("DEBUG Vecchio IP: "+lastIP,null);
		//String newIP=getIP();
		String newIP=newip.trim();
		log("DEBUG Nuovo IP : "+newIP,null);
		if (newIP.equals(lastIP)){
			updatestatus.setText("L'IP non è cambiato non si necessita di aggiornare.");
			log("L'IP non è cambiato non si necessita di aggiornare.",null);
		}else if(newIP.equals("<html><font color=\"red\">Errore, verifica il log</font></html>")){
			updatestatus.setText("<html><font color=\"red\">Errore, verifica il Log.</font></html>");
			log("--------errore in fase di prelievo IP------------",null);
		}else{
			updatestatus.setText("L'IP è cambiato, procedo ad aggiornare...");
			log("------------inizio procedura di update---------------",null);
			String result="";
			update=false;
			try{
			result = http(
				" https://www.dtdns.com/api/autodns.cfm?id=" + id + "&pw=" + pass + "&ip=" + newIP);
			log(" https://www.dtdns.com/api/autodns.cfm?id=" + id + "&pw=**********" + "&ip=" + newIP, null);
			log(newIP + "," + result.trim(), null);
			String checkerror=result.trim()+"|";
			String []spliterror=checkerror.split("|");
			String error=spliterror[0]+spliterror[1]+spliterror[2];
			if(!error.equals("Too")&&!error.equals("The")&&!error.equals("Err")&&!error.equals("Inv")){
				update=true;
			}
			
			}catch(Exception e){
				log("Update fallito!",e);
				result="Errore, vedi log!: "+result;
				log(result, null);
			}
			
			System.out.println(result);
			if (update==true) {
				updatestatus.setText("<html><font color=\"green\">Aggiornato con successo.</font></html>");
				lastIP = newIP;
				ipaddress.setText("<html><font color=\"green\">"+newip+"</font></html>");
				lastipaddress.setText("<html><font color=\"green\">"+newIP+"</font></html>");
				saveConfig();
			}else{
				updatestatus.setText("<html><font color=\"red\">Errore, verifica il Log.</font></html>");
			}
		}
	}

	String http (String url) throws IOException {
		try{
		InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
		StringWriter writer = new StringWriter(128);
		char[] buffer = new char[1024];
		while (true) {
			int count = reader.read(buffer);
			if (count == -1) break;
			writer.write(buffer, 0, count);
		}
		return writer.toString();
			/*
			StringBuilder result = new StringBuilder();
		      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		      conn.setRequestMethod("GET");
		      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		      String line;
		      while ((line = rd.readLine()) != null) {
		         result.append(line);
		      }
		      rd.close();
		      return result.toString();
		   	*/
		}catch(ConnectException e){
			log("Errore in fase di connessione a DTDNS: "+e,null);
			String error="Errore di connesisone a DTDNS";
			return error;
		}
	}

	void saveConfig () throws IOException {
		FileWriter writer = new FileWriter(configFile);
		writer.write("User: " + user + "\r\n");
		writer.write("Password: " + pass + "\r\n");
		writer.write("Record ID: " + id + "\r\n");
		writer.write("Minutes: " + minutes + "\r\n");
		writer.write("Last IP: " + lastIP + "\r\n");
		writer.write("Exit: " + exit);
		writer.close();
	}

	void loadConfig () throws IOException {
		//if (!configFile.exists()) saveConfig();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			user = value(reader.readLine());
			pass = value(reader.readLine());
			id = value(reader.readLine());
			minutes = Integer.parseInt(value(reader.readLine()));
			lastIP = value(reader.readLine());
			exit = Boolean.parseBoolean(value(reader.readLine()));
			reader.close();
		} catch (Exception ex) {
			throw new RuntimeException("Error reading config file: " + configFile.getAbsolutePath(), ex);
		}
		
	}

	String value (String line) {
		int index = line.indexOf(":");
		if (index == -1) throw new RuntimeException("Invalid line: " + line);
		String value = line.substring(index + 1).trim();
		if (value.length() > 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
			value = value.substring(1, value.length() - 2);
		return value;
	}

	static class MultiplexOutputStream extends OutputStream {
		private final OutputStream[] streams;

		public MultiplexOutputStream (OutputStream... streams) {
			if (streams == null) throw new IllegalArgumentException("streams cannot be null.");
			this.streams = streams;
		}

		public void write (int b) throws IOException {
			for (int i = 0; i < streams.length; i++) {
				synchronized (streams[i]) {
					streams[i].write(b);
				}
			}
		}

		public void write (byte[] b, int off, int len) throws IOException {
			for (int i = 0; i < streams.length; i++) {
				synchronized (streams[i]) {
					streams[i].write(b, off, len);
				}
			}
		}
	}

	static void log (String message, Throwable ex) {
		if (ex != null) {
			StringWriter buffer = new StringWriter(1024);
			ex.printStackTrace(new PrintWriter(buffer));
			message += "\n" + buffer.toString();
		}
		System.out.println(dateFormat.format(new Date()) + " " + message);
	}
	
	public String getIP(){
		String newIP="";
		try {
			newIP = http("http://www.dnsmadeeasy.com/myip.jsp");
		} catch (IOException ex) {
			log("Error obtaining IP.", ex);
			newIP="<html><font color=\"red\">Errore, verifica il log</font></html>";
			return newIP;
		}
		return newIP;	
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	JButton test =(JButton) e.getSource();
	if(test.equals(btnRinnovaDns)){
			 Runnable r = new Runnable() {
		         public void run() { 
		        	 try {
						update(id,pass );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		         }
		     };	
		     Thread rn= new Thread(r);
		     if((rn.getState()!=Thread.State.TERMINATED)){
		    	 new Thread(r).start();
		    	 log("DEBUG: sono dentro l'if quindi r è terminato",null);
		    	 }else{
		    		 JOptionPane.showMessageDialog(null, "Aggiornamento già in corso,\nAttendi..", "InfoBox: " + "Attenzione!", JOptionPane.INFORMATION_MESSAGE);	     
		    	 }
		     
	}else if(test.equals(btnRicontrollaIP)){
		
		Runnable t = new Runnable() {
	         public void run() { 
	        		 checkip=getIP();
	        		 if(checkip!="<html><font color=\"red\">Errore, verifica il log</font></html>"&&checkip!=lastIP){
	        			 newip=checkip;
	        			 
	        			 updatestatus.setText("<html><font color=\"#f9af1b\">Rilevato cambio di IP, Aggiorno...</font></html>");
	        			 ipaddress.setText("<html><font color=\"green\">"+newip+"</font></html>");
	        			 try {
							update(id,pass );
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        		 }
				}
	         };
	     	
		     Thread rn= new Thread(t);
		     if((rn.getState()!=Thread.State.TERMINATED)){
		    	 new Thread(t).start();
		    	 log("DEBUG: sono dentro l'if quindi r è terminato",null);
		    	 }else{
		    		 JOptionPane.showMessageDialog(null, "Aggiornamento già in corso,\nAttendi..", "InfoBox: " + "Attenzione!", JOptionPane.INFORMATION_MESSAGE);	     
		    	 }
		    	
		}
		
	}
	
}
