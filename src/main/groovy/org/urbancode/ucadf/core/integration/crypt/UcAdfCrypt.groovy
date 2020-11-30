package org.urbancode.ucadf.core.integration.crypt

import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import java.security.spec.KeySpec

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import groovy.json.StringEscapeUtils
import groovy.util.logging.Slf4j

// To create a keystore.
// keytool -genseckey -alias 256bitkey -keyalg aes -keysize 256 -keystore encryption.keystore -storetype jceks
//
// To generate a key in a keystore.
// keytool -genseckey -keystore encryption.keystore -storetype jceks -storepass myKeystorePass -keyalg AES -keysize 256 -alias mykey -keypass myKeyPass
//	
// To list the keys in a keystore.
// keytool -list -keystore encryption.keystore -storepass password -storetype jceks
@Slf4j
class UcAdfCrypt {
	public final static String DEFAULT_KEYSTORETYPE = "JCEKS"
	public final static String DEFAULT_KEYALGORITHM = "AES"
	public final static String DEFAULT_CIPHERTRANSFORMATION = "AES/CBC/PKCS5Padding"
	public final static String DEFAULT_KEYGEN_ALGORITHM = "PBKDF2WithHmacSHA256"
	
	String keyAlgorithm
	String cipherTransformation

	// Keystore information.
	File keystore
	String keystoreType
	String keystorePass
	String keyAlias
	String keyPass
	
	// Encoded key information.
	String encodedKey
	
	// The initialiation vector.
	String encodedIv
	
	// The secret key.
	Key secretKey

	private Cipher cipher
	
	// Constructors.	
	public Cipher getCipherInstance() {
		// Get a new cipher instance.
		if (!cipher) {
			if (!cipherTransformation) {
				throw new UcAdfInvalidValueException("No value for cipherTransformation provided.")
			}
			
			cipher = Cipher.getInstance(cipherTransformation)
		}
		
		return cipher
	}
	
	// Derive the secret key value. If an encoded key was provided then use it. Othwerwise, get the key from the keystore.
	public deriveSecretKey() {
		if (encodedKey) {
			// Convert base 64 encoded string to a secret key.
			byte[] decodedKey = Base64.getDecoder().decode(encodedKey.toString())
			secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, keyAlgorithm)
		} else {
			if (!keystore) {
				throw new UcAdfInvalidValueException("No value for keystore provided.")
			}
			
			if (!keystoreType) {
				throw new UcAdfInvalidValueException("No value for keystore type provided.")
			}
	
			if (!keystorePass) {
				throw new UcAdfInvalidValueException("No value for keystore password provided.")
			}
			
			if (!keyAlias) {
				throw new UcAdfInvalidValueException("No value for key alias provided.")
			}
	
			if (!keyPass) {
				throw new UcAdfInvalidValueException("No value for key password provided.")
			}
			
			InputStream keystoreStream = new FileInputStream(keystore)
	
			KeyStore keystoreInstance = KeyStore.getInstance(keystoreType)
			
			keystoreInstance.load(
				keystoreStream, 
				keystorePass.toCharArray()
			)
			
			if (!keystoreInstance.containsAlias(keyAlias)) {
				throw new RuntimeException("Alias for key not found")
			}

			secretKey = keystoreInstance.getKey(keyAlias, keyPass.toCharArray())
		}
	}

	// Generate a key.
	public Key generateKey(
		final String password,
		final String salt,
		final String algorithm) {
		
		KeySpec keySpec = new PBEKeySpec(
			password.toCharArray(), 
			salt.getBytes(), 
			65536, 
			256
		)
		
		return SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec)
	}

	// Convert secret key to base 64 encoded string.
	public String getEncodedKey(final SecretKey secretKey) {
		return Base64.getEncoder().encodeToString(secretKey.getEncoded())
	}
	
	// Set up a random initialization vector for encryption.
	public generateEncodedIv() {
		int ivSize = getCipherInstance().getBlockSize()
		byte[] ivBytes = new byte[ivSize]
		SecureRandom random = new SecureRandom()
		random.nextBytes(ivBytes)
		
		encodedIv = Base64.getEncoder().encodeToString(ivBytes)
	}	

	// Initialize a cipher for encryption/decryption.
	public Cipher initCipher(final int cipherMode) {
		if (!secretKey) {
			throw new UcAdfInvalidValueException("No value for secret key provided.")
		}
		
		if (!keyAlgorithm) {
			throw new UcAdfInvalidValueException("No value for key algorithm provided.")
		}

		if (!encodedIv) {
			throw new UcAdfInvalidValueException("No value for IV provided.")
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(
			secretKey.getEncoded(), 
			keyAlgorithm
		)

		IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.getDecoder().decode(encodedIv))

		// Initialize the cipher instance.
		getCipherInstance().init(
			cipherMode, 
			secretKeySpec, 
			ivParameterSpec
		)

		return cipher
	}	
	
	// Encrypt a string.
	public String encrypt(final String value) {
		// If no encoded IV has been set then generate one.
		if (!encodedIv) {
			generateEncodedIv()
		}
		
		// Derive the secret key using the information set to this point.
		deriveSecretKey()
		
		return Base64.getEncoder().encodeToString(
			initCipher(Cipher.ENCRYPT_MODE).doFinal(value.getBytes("UTF-8"))
		)
	}

	// Decrypt a string. Valid formats:
	// 	crypt_v1{transformation|keyAlias|encodedIV|value}
	// 	transformation|keyAlias|encodedIV|value
	// 	keyAlias|encodedIV|value
	// 	encodedIV|value
	// 	value
	public String decrypt(final String decryptStr) {
		String derivedValue = decryptStr.toString().replaceAll(/^crypt_v1\{(.*?)\}$/, '$1')
		List<String> segments = StringEscapeUtils.unescapeJava(derivedValue.toString()).split(/\|/)
		
		String value
		switch (segments.size()) {
			case 4:
				cipherTransformation = segments[0]
				keyAlias = segments[1]
				encodedIv = segments[2]
				value = segments[3]
				break
			case 3:
				keyAlias = segments[0]
				encodedIv = segments[1]
				value = segments[2]
				break
			case 2:
				encodedIv = segments[0]
				value = segments[1]
				break
			case 1:
				value = segments[0]
				break
			default:
				throw new UcAdfInvalidValueException("Don't know how to handle value with [${segments.size()}] segments.")
		}

		// Derive the secret key using the information set to this point.
		deriveSecretKey()

        return new String(
			initCipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(value))
		)
    }
}
