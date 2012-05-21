package com.gnuc.thoth.framework.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

public class ThothSSLSocketFactory extends SSLSocketFactory
{
	SSLContext	sslContext	= SSLContext.getInstance("TLS");
	
	public ThothSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
	{
		super(truststore);
		TrustManager tm = new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{}
			
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{}
		};
		sslContext.init(null, new TrustManager[]{tm}, null);
	}
	
	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException
	{
		return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}
	
	@Override
	public Socket createSocket() throws IOException
	{
		return sslContext.getSocketFactory().createSocket();
	}
}
