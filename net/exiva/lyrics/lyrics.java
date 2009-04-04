package net.exiva.lyrics;

import danger.app.*;
import danger.net.*;
import java.io.*;
import danger.player.MusicPlayer;
import danger.player.AudioTrack;
import danger.ui.*;
import java.net.URLEncoder;
import danger.util.MetaStrings;
import danger.util.DEBUG;
import org.kxml2.io.KXmlParser;

public class lyrics extends Application implements Resources, Commands {
	static public lyricsView mWindow;

	public static SettingsDB lyricPrefs;
	private final String LYRICS = "lyrics";
	private final String ARTIST = "artist";
	private final String SONG = "song";
	public static String artist;
	public static String song;
	public static String lyrics;
	public static String version;
	public static String build;
	public static String className;
	public static int callHome;

	public lyrics() {
		mWindow = lyricsView.create();
		mWindow.show();
		version = getString(ID_VER);
		build = getString(ID_BUILD);
	}

	public void launch() {
		restoreData();
		checkUpdate();
	}

	public void resume() {
		getInfo();
	}

	public void restoreData() {
		if (SettingsDB.findDB("lyricSettings") == false) {
			lyricPrefs = new SettingsDB("lyricSettings", true);
			lyricPrefs.setAutoSyncNotifyee(this);
			callHome();
		} else {
			lyricPrefs = new SettingsDB("lyricSettings", true);
			try {
				callHome = lyricPrefs.getIntValue("callHome");
			} catch (SettingsDBException exception) {}
			if (callHome != 1) {
				callHome();
			}
		}
	}

	public static void checkUpdate() {
		HTTPConnection.get("http://static.tmblr.us/hiptop/updateCheck.php?a=2&v="+version+"&b="+build, null, (short) 0, 2);
	}

	public void callHome() {
		HTTPConnection.get("http://static.tmblr.us/hiptop/hiptopLog2.php?a="+getBundle().getClassName()+"&n="+MetaStrings.get(MetaStrings.ID_PARTNER_NAME)+"&d="+MetaStrings.get(MetaStrings.ID_DEVICE_MODEL)+"&b="+MetaStrings.get(MetaStrings.ID_BRAND_NAME)+"&u="+HiptopConnection.getUserName(), null, (short) 0, 99);
		lyricPrefs.setIntValue("callHome", 1);
	}

	public static void searchLyrics(String artist, String song) {
		try { artist = URLEncoder.encode(artist, "UTF-8");
			  song = URLEncoder.encode(song, "UTF-8"); }
		catch (UnsupportedEncodingException e) { }
 		HTTPConnection.get("http://lyricwiki.org/api.php?artist="+artist+"&song="+song+"&fmt=text", null, (short) 0, 1);
	}
	
	public static void sendMail(String artist, String song) {
		IPCMessage ipc = new IPCMessage();
		ipc.addItem("action" , "send");
		ipc.addItem("subject" , "Lyrics for: "+artist+" "+song);
		ipc.addItem("body" , lyrics);
		Registrar.sendMessage("Email", ipc, null);
	}
	
	public static void getInfo() {
		if (MusicPlayer.isPlaying()) {
			try {
				//thanks Joe! :D
				AudioTrack audio = MusicPlayer.getCurrentTrack();
				mWindow.setTrackInfo(audio.getArtist(), audio.getTitle());
				} catch (Exception exception) {}
			}
	}
	
/*	private void parseXML(String source)
	{
		//thanks jsav!
		DEBUG.p("Parsing XML...");
		String tagname, text;
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(source.getBytes());
			InputStreamReader in = new InputStreamReader(bin);
			KXmlParser parser = new KXmlParser();
			parser.setInput(in);
			do {
				tagname = parser.getName();
				text = parser.getText();
				if (LYRICS.equals(tagname)) {
					text = parser.nextText();
					mWindow.setLyrics(text);
				}
				} while (parser.nextToken() != parser.END_DOCUMENT);
			} catch (Exception ex) {
				DEBUG.p("caught "+ex+" on Lyrics");
			}
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(source.getBytes());
			InputStreamReader in = new InputStreamReader(bin);
			KXmlParser parser = new KXmlParser();
			parser.setInput(in);
			do {
				tagname = parser.getName();
				text = parser.getText();
				if (ARTIST.equals(tagname)) {
					text = parser.nextText();
					artist = text;
					DEBUG.p("lyrics: Artist: "+text);
				}
				} while (parser.nextToken() != parser.END_DOCUMENT);
			} catch (Exception ex) {
				DEBUG.p("caught "+ex+" on Artist");
			}
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(source.getBytes());
			InputStreamReader in = new InputStreamReader(bin);
			KXmlParser parser = new KXmlParser();
			parser.setInput(in);
			do {
				tagname = parser.getName();
				text = parser.getText();
				if (SONG.equals(tagname)) {
					text = parser.nextText();
					song = text;
					DEBUG.p("lyrics: Song Title: "+text);
				}
				} while (parser.nextToken() != parser.END_DOCUMENT);
			} catch (Exception ex) {
				DEBUG.p("caught "+ex+" on Song");
			}	
		mWindow.setSubTitle(artist+" \""+song+"\"");
	}*/
	
	public boolean receiveEvent(Event e) {
		switch (e.type) {
			case Event.EVENT_DATASTORE_RESTORED: {
				restoreData();
				return true;
			}
		}
		return super.receiveEvent(e);
	}

	public void networkEvent(Object object) {
		if (object instanceof HTTPTransaction)
		{
			HTTPTransaction trans = (HTTPTransaction) object;
			int seqID = trans.getSequenceID();
			if (seqID == 1) {
//			parseXML(trans.getString());
			mWindow.setLyrics(trans.getString());
			lyrics=trans.getString();
			} else if (seqID == 2) {
				if (!"false".equals(trans.getString())) {
					mWindow.setSubTitle("Update Available!");
					MarqueeAlert updateAlert = new MarqueeAlert(trans.getString(), getBundle().getSmallIcon(), 1);
					NotificationManager.marqueeAlertNotify(updateAlert);
				}
			}
			trans = null;
		}
	}
} // NOM NOM NOM