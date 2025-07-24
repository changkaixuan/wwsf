package com.bocsoft.wwsf.webconsole;

import junit.framework.TestCase;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

public class SshClientUtilsTest extends TestCase {

    public static KeyPair loadPrivateKey(String privateKeyPath, String password ) throws Exception{
        File privateKeyFile = new File(privateKeyPath);
        PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
        Object o = pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        if(o instanceof PEMEncryptedKeyPair){
            if (password == null){
                throw new IllegalStateException("需要私钥密码");
            }
            PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
            PEMKeyPair decryptKeyPair = ((PEMEncryptedKeyPair)o).decryptKeyPair(decryptorProvider);
            return converter.getKeyPair(decryptKeyPair);
        }
        if(o instanceof PEMKeyPair){
            return converter.getKeyPair((PEMKeyPair) o);
        }

        if(o instanceof KeyPair){
            return (KeyPair)o;
        }
        throw  new IllegalStateException("不支持的密钥格式");
    }

    @Test
    public void testInstall() {
        try {
            SshClient client = SshClient.setUpDefaultClient();
            client.start();
            ConnectFuture connectFuture = client.connect("wwsf", "50.131.232.114", 10022);
            connectFuture.await();
            if (!connectFuture.isConnected()) {
                System.out.println("连接失败！！！！！");
            }else {
                System.out.println("连接成功！！！！！");
                ClientSession session = connectFuture.getSession();

                // 密码认证
                //session.addPasswordIdentity("Qw@3124!");

                // ======================SSH公钥认证=========================== //
                KeyPair keyPair = loadPrivateKey("D:\\wws\\wwsf\\id_rsa","Er@4235!");
                if(keyPair == null ){
                    System.out.println("无法加载私钥");
                    return;
                }
                session.addPublicKeyIdentity(keyPair);
                // ======================SSH 公钥认证=========================== //

                AuthFuture authTrue = session.auth();
                authTrue.await();
                if (!authTrue.isSuccess()) {
                    System.out.println( "用户名：wws ,公钥认证,验证失败");
                }else{
                    System.out.println( "用户名：wws ,公钥认证,验证成功！！！");
                    String extcResult = session.executeRemoteCommand("ls ", new ByteArrayOutputStream(), Charset.forName("UTF-8"));
                    System.out.println("输出：" + extcResult);

                }
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            System.out.println("执行结束");
        }
    }

    @Test
    public void testUnInstall(){
        try {
            // 生成 RSA 密钥对（4096 位）
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // 将私钥和公钥转换为 PEM 格式
            String privateKeyPEM = toPEM(privateKey);
            String publicKeyPEM = toPEM(publicKey);

            System.out.println("Private Key (PEM):");
            System.out.println(privateKeyPEM);
            System.out.println("\nPublic Key (PEM):");
            System.out.println(publicKeyPEM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String toPEM(java.security.Key key) {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN ");
        if (key instanceof RSAPrivateKey) {
            sb.append("PRIVATE KEY");
        } else if (key instanceof RSAPublicKey) {
            sb.append("PUBLIC KEY");
        }
        sb.append("-----\n");
        sb.append(Base64.getEncoder().encodeToString(key.getEncoded()));
        sb.append("\n-----END ");
        if (key instanceof RSAPrivateKey) {
            sb.append("PRIVATE KEY");
        } else if (key instanceof RSAPublicKey) {
            sb.append("PUBLIC KEY");
        }
        sb.append("-----\n");
        return sb.toString();
    }


}