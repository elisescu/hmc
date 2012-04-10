
package net.java.otr4j.test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrException;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.OtrEngineImpl;
import net.java.otr4j.OtrPolicyImpl;
import net.java.otr4j.session.SessionID;
import net.java.otr4j.session.SessionStatus;
public class OtrEngineImplTest {
  private net.java.otr4j.session.SessionID aliceSessionID =  new SessionID("Alice@Wonderland",
			"Bob@Wonderland", "Scytale");

  private net.java.otr4j.session.SessionID bobSessionID =  new SessionID("Bob@Wonderland",
			"Alice@Wonderland", "Scytale");

  private static Logger logger =  Logger.getLogger("ElisLogger");

  class DummyOtrEngineHost implements net.java.otr4j.OtrEngineHost {
    public DummyOtrEngineHost(net.java.otr4j.OtrPolicy policy, String hostname) {
			this.hostName = hostname;
			this.policy = policy;
    }

    private net.java.otr4j.OtrPolicy policy;

    public String lastInjectedMessage;

    private String hostName;

    public net.java.otr4j.OtrPolicy getSessionPolicy(net.java.otr4j.session.SessionID ctx) {
			return this.policy;
    }

    public void injectMessage(net.java.otr4j.session.SessionID sessionID, String msg) {
			this.lastInjectedMessage = msg;
			String msgDisplay = (msg.length() > 100) ? msg.substring(0, 100)
					+ "..."+(msg.length()-100)+" more" : msg;

			System.out.println(hostName+": IM injects message: " + msgDisplay);
			logger.finest(hostName+": IM injects message: " + msgDisplay);
    }

    public void showError(net.java.otr4j.session.SessionID sessionID, String error) {
			System.out.println(hostName+": IM shows error to user: " + error);
			logger.severe(hostName+": IM shows error to user: " + error);
    }

    public void showWarning(net.java.otr4j.session.SessionID sessionID, String warning) {
			System.out.println(hostName+": IM shows warning to user: " + warning);
				logger.warning(hostName+": IM shows warning to user: " + warning);
    }

    public void sessionStatusChanged(net.java.otr4j.session.SessionID sessionID) {
			// don't care.
    }

    public KeyPair getKeyPair(net.java.otr4j.session.SessionID paramSessionID) {
			KeyPairGenerator kg;
			try {
				kg = KeyPairGenerator.getInstance("DSA");

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			}

			return kg.genKeyPair();
    }

  }

  private net.java.otr4j.OtrEngineImpl usAlice;

  private net.java.otr4j.OtrEngineImpl usBob;

  private DummyOtrEngineHost hostAlice;

  private DummyOtrEngineHost hostBob;

  public void startSession() throws net.java.otr4j.OtrException {
		hostAlice = new DummyOtrEngineHost(new OtrPolicyImpl(OtrPolicy.ALLOW_V2
				| OtrPolicy.ERROR_START_AKE), "hostAlice");
		
		hostBob =  new DummyOtrEngineHost(new OtrPolicyImpl(OtrPolicy.ALLOW_V2
				| OtrPolicy.ERROR_START_AKE), "hostBob");

		usAlice = new OtrEngineImpl(hostAlice);
		usBob = new OtrEngineImpl(hostBob);

		System.out.println("usAlice.startSession(aliceSessionID);");
		try {
			usAlice.startSession(aliceSessionID);
		} catch (OtrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Bob receives query, sends D-H commit.");
		usBob.transformReceiving(bobSessionID, hostAlice.lastInjectedMessage);

		System.out.println("Alice received D-H Commit, sends D-H key.");
		usAlice.transformReceiving(aliceSessionID,
				hostBob.lastInjectedMessage);

		System.out.println("Bob receives D-H Key, sends reveal signature.");
		usBob.transformReceiving(bobSessionID, hostAlice.lastInjectedMessage);

		System.out.println("Alice receives Reveal Signature, sends signature and goes secure.");
		usAlice
		.transformReceiving(aliceSessionID,
				hostBob.lastInjectedMessage);

		System.out.println("Bobs receives Signature, goes secure.");
		usBob.transformReceiving(bobSessionID, hostAlice.lastInjectedMessage);

		if (usBob.getSessionStatus(bobSessionID) != SessionStatus.ENCRYPTED
				|| usAlice.getSessionStatus(aliceSessionID) != SessionStatus.ENCRYPTED)
			fail("Could not establish a secure session.");
  }

  private void fail(String string) {
		System.out.print("EEEEEROR "+string);
  }

  private void fail() {
		System.out.print("EEEEEROR ");
  }

  public void exchageMessages() throws net.java.otr4j.OtrException {
		// We are both secure, send encrypted message.
		String clearTextMessage = "Hello Bob, this new IM software you installed on my PC the other day says we are talking Off-the-Record, what is that supposed to mean?";
		String sentMessage = usAlice.transformSending(aliceSessionID,
				clearTextMessage);

		// Receive encrypted message.
		String receivedMessage = usBob.transformReceiving(bobSessionID,
				sentMessage);

		if (!clearTextMessage.equals(receivedMessage))
			fail();

		// Send encrypted message.
		clearTextMessage = "Hey Alice, it means that our communication is encrypted and authenticated.";
		sentMessage = usBob.transformSending(bobSessionID, clearTextMessage);

		// Receive encrypted message.
		receivedMessage = usAlice.transformReceiving(aliceSessionID,
				sentMessage);

		if (!clearTextMessage.equals(receivedMessage))
			fail();

		// Send encrypted message.
		clearTextMessage = "Oh, is that all?";
		sentMessage = usAlice
				.transformSending(aliceSessionID, clearTextMessage);

		// Receive encrypted message.
		receivedMessage = usBob.transformReceiving(bobSessionID, sentMessage);
		if (!clearTextMessage.equals(receivedMessage))
			fail();

		// Send encrypted message.
		clearTextMessage = "Actually no, our communication has the properties of perfect forward secrecy and deniable authentication.";
		sentMessage = usBob.transformSending(bobSessionID, clearTextMessage);

		// Receive encrypted message.
		receivedMessage = usAlice.transformReceiving(aliceSessionID,
				sentMessage);

		if (!clearTextMessage.equals(receivedMessage))
			fail();

		// Send encrypted message. Test UTF-8 space characters.
		clearTextMessage = "Oh really?! pouvons-nous parler en français?";

		sentMessage = usAlice
				.transformSending(aliceSessionID, clearTextMessage);

		// Receive encrypted message.
		receivedMessage = usBob.transformReceiving(bobSessionID, sentMessage);
		if (!clearTextMessage.equals(receivedMessage))
			fail();
  }

  private void endSession() throws net.java.otr4j.OtrException {
		usBob.endSession(bobSessionID);
		usAlice.endSession(aliceSessionID);

		if (usBob.getSessionStatus(bobSessionID) != SessionStatus.PLAINTEXT
				|| usAlice.getSessionStatus(aliceSessionID) != SessionStatus.PLAINTEXT)
			fail("Failed to end session.");
  }

  public static void main(String[] args)
  {
		OtrEngineImplTest  test = new OtrEngineImplTest();    

		System.out.print("Otr Session !!::  \n\n");
		try {
			test.startSession();
			test.exchageMessages();
			test.endSession();
		} catch (OtrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }

}
