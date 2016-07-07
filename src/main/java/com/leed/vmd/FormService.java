package com.leed.vmd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.leed.vmd.data.Registration;

public class FormService
{

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "vmd";

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart.json
     */
    private static final List<String> SCOPES = Arrays
            .asList( SheetsScopes.SPREADSHEETS_READONLY, SheetsScopes.SPREADSHEETS );

    static
    {
        try
        {
            final NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
            builder.trustCertificates( GoogleUtils.getCertificateTrustStore() );
            builder.setProxy( new Proxy( Proxy.Type.HTTP, new InetSocketAddress( "127.0.0.1", 3128 ) ) );

            HTTP_TRANSPORT = builder.build();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            System.exit( 1 );
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException
    {
        return GoogleCredential.fromStream(
                FormService.class.getResourceAsStream( "/vmd-service.json" ) ).createScoped( SCOPES );
    }

    /**
     * Build and return an authorized Sheets API client service.
     *
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException
    {
        final Credential credential = authorize();
        return new Sheets.Builder( HTTP_TRANSPORT, JSON_FACTORY, credential )
                .setApplicationName( APPLICATION_NAME )
                .build();
    }

    public static void main( String[] args ) throws IOException, NoSuchAlgorithmException, KeyManagementException
    {
        trustEveryone();

        // Build a new authorized API client service.
        final Sheets service = getSheetsService();

        final ValueRange response = service.spreadsheets().values().get( spreadsheetId, range ).execute();
        List<List<Object>> values = response.getValues();
        if ( values == null || values.size() == 0 )
        {
            System.out.println( "No data found." );
            values = new ArrayList<>();
        }
        else
        {
            System.out.println( "Name, Major" );
            for ( final List row : values )
            {
                System.out.printf( "%s, %s\n", row.get( 0 ), row.get( 1 ) );
            }
        }

        final List<Object> objects = new ArrayList<>();
        objects.add( "a" );
        objects.add( "b" );
        values.add( objects );

        service.spreadsheets().values().update( spreadsheetId, range, response ).setValueInputOption( "USER_ENTERED" )
                .execute();
    }

    private static final String spreadsheetId = "1HEOCsUkt-5GDzT-1sHevz2YmM0nSUIpB19C0VJA2M0Q";
    private static final String range = "Registrations!A2:E30";

    public Registration complete( final String recaptcha, final String body )
            throws KeyManagementException, NoSuchAlgorithmException, IOException
    {
        trustEveryone();

        final Registration registration = Bootstrap.getGson().fromJson( body, Registration.class );

        if ( !VerifyRecaptcha.verify( recaptcha ) )
        {
            return registration;
        }

        // Build a new authorized API client service.
        final Sheets service = getSheetsService();

        final ValueRange response = service.spreadsheets().values().get( spreadsheetId, range ).execute();

        final List<List<Object>> values = response.getValues() == null ? new ArrayList<>() : response.getValues();

        // Add the new registration to the end
        final List<Object> objects = new ArrayList<>();
        objects.add( registration.getName() );
        objects.add( registration.getBusinessName() );
        objects.add( registration.getEmail() );
        objects.add( registration.getMobile() );

        values.add( objects );

        service.spreadsheets().values().update( spreadsheetId, range, response ).setValueInputOption( "USER_ENTERED" )
                .execute();

        return registration;
    }

    private static void trustEveryone() throws NoSuchAlgorithmException, KeyManagementException
    {
        System.setProperty( "http.proxyHost", "127.0.0.1" );
        System.setProperty( "http.proxyPort", "3128" );
        System.setProperty( "https.proxyHost", "127.0.0.1" );
        System.setProperty( "https.proxyPort", "3128" );

        final TrustManager[] trustAllCerts = new TrustManager[]
                {
                        new X509TrustManager()
                        {
                            @Override
                            public void checkClientTrusted( X509Certificate[] x509Certificates, String s )
                                    throws CertificateException
                            {

                            }

                            @Override
                            public void checkServerTrusted( X509Certificate[] x509Certificates, String s )
                                    throws CertificateException
                            {

                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers()
                            {
                                return null;
                            }
                        }
                };

        final SSLContext sc = SSLContext.getInstance( "TLS" );
        sc.init( null, trustAllCerts, new java.security.SecureRandom() );

        final HostnameVerifier allHostsValid = ( str, sslSession ) -> true;

        HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
        HttpsURLConnection.setDefaultHostnameVerifier( allHostsValid );
    }
}
