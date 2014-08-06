package org.wso2.carbon.humantask.core.engine;

import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

/**
 * 
 * @author admin
 */
public class SendMessage {
	private String message;
	private String telephoneno;
	private String gateway;
	private String port;
	private int baudRate;
	private String dongleManufacturer;
	private String dongleModel;
	private String smscNumber;
	
	public SendMessage( String message,String telephoneno,String gateway,String port,int baudRate,String dongleManufacturer,String dongleModel,String smscNumber){
		setMessage(message, telephoneno);
		setDongleConfigs(gateway, port, baudRate, dongleManufacturer, dongleModel,smscNumber);
	}

	public void setMessage(String message, String telephoneno) {
		this.message = message;
		this.telephoneno = telephoneno;
	}
	public void setDongleConfigs(String gateway,String port, int baudRate, String dongleManufacturer, String dongleModel,String smscNumber){
		this.gateway=gateway;
		this.port=port;
		this.baudRate=baudRate;
		this.dongleManufacturer=dongleManufacturer;
		this.dongleModel=dongleModel;
		this.smscNumber=smscNumber;
	}
	
	public void doIt() throws Exception {
		// String port=new GetPort().comport();

		OutboundNotification outboundNotification = new OutboundNotification();
		System.out.println("Example: Send message from a serial gsm modem.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());
		System.setProperty("smslib.serial.polling", "true");
		
		
		SerialModemGateway gateway = new SerialModemGateway(this.gateway, port, baudRate,
				"Huawei", "E3131");
		
		gateway.setInbound(true);
		gateway.setOutbound(true);
		gateway.setSimPin("0000");

		// Explicit SMSC address set is required for some modems.
		gateway.setSmscNumber(smscNumber); //The SMSC Number is "+947100003" for Mobitel Sri Lanka, "+9471000003" for Dialog Sri Lanka
		Service s=new Service();
		s.setOutboundMessageNotification(outboundNotification);
		s.addGateway(gateway);
		s.startService();
		
		System.out.println("Modem Information:");
		System.out.println("  Manufacturer: " + gateway.getManufacturer());
		System.out.println("  Model: " + gateway.getModel());
		System.out.println("  Serial No: " + gateway.getSerialNo());
		System.out.println("  SIM IMSI: " + gateway.getImsi());
		System.out.println("  Signal Level: " + gateway.getSignalLevel() + " dBm");
		System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
		System.out.println();
		// Send a message synchronously.
		OutboundMessage msg = new OutboundMessage(telephoneno, message);
		s.sendMessage(msg);
		System.out.println(msg);
		//System.out.println("Now Sleeping - Hit <enter> to terminate.");
		//System.in.read();
		s.stopService();
	}

	public class OutboundNotification implements IOutboundMessageNotification {
		public void process(AGateway gateway, OutboundMessage msg) {
			System.out.println("Outbound handler called from Gateway: " + gateway.getGatewayId());
			System.out.println(msg);
		}
	
		public void process(String arg0, OutboundMessage arg1) {
			// TODO Auto-generated method stub
			
		}
	}

}