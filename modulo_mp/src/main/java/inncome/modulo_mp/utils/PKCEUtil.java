package inncome.modulo_mp.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidades para la implementación del flujo PKCE (Proof Key for Code Exchange).
 *
 * <p>Esta clase proporciona métodos estáticos para generar y procesar los elementos
 * requeridos en el flujo PKCE de OAuth 2.0, específicamente el code verifier y
 * code challenge utilizados para mejorar la seguridad en aplicaciones públicas.</p>
 *
 * <p>El flujo PKCE incluye:</p>
 * <ul>
 *   <li>Generación de un code verifier aleatorio y seguro</li>
 *   <li>Creación del code challenge mediante hash SHA-256</li>
 *   <li>Codificación Base64 URL-safe sin padding</li>
 * </ul>
 *
 * <p>Este mecanismo previene ataques de intercepción de códigos de autorización
 * en aplicaciones que no pueden mantener secretos de cliente de forma segura.</p>
 *
 * @see <a href="https://tools.ietf.org/html/rfc7636">RFC 7636 - PKCE</a>
 * @since 1.0
 */
public class PKCEUtil {

    /**
     * Genera un code verifier aleatorio para el flujo PKCE.
     *
     * <p>Este metodo crea un código verificador criptográficamente seguro de 64 bytes
     * aleatorios, codificado en Base64 URL-safe sin padding. El code verifier es
     * utilizado posteriormente para intercambiarlo por el access token en el flujo
     * de autorización OAuth 2.0.</p>
     *
     * <p>Características del code verifier generado:</p>
     * <ul>
     *   <li>64 bytes de datos aleatorios seguros</li>
     *   <li>Codificación Base64 URL-safe (compatible con URLs)</li>
     *   <li>Sin padding para cumplir con la especificación PKCE</li>
     *   <li>Generado con SecureRandom para garantizar la aleatoriedad</li>
     * </ul>
     *
     * @return String el code verifier codificado en Base64 URL-safe, listo para usar
     *         en el flujo de autorización
     *
     * @see #generateCodeChallenge(String)
     * @since 1.0
     */
    public static String generateCodeVerifier() {
        byte[] random = new byte[64];
        new SecureRandom().nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    /**
     * Genera el code challenge a partir del code verifier utilizando SHA-256.
     *
     * <p>Este metodo toma un code verifier y genera su code challenge correspondiente
     * mediante el algoritmo de hash SHA-256. El code challenge es enviado en la
     * solicitud de autorización inicial, mientras que el code verifier se utiliza
     * posteriormente para intercambiar el código de autorización por el access token.</p>
     *
     * <p>Proceso de generación:</p>
     * <ul>
     *   <li>Conversión del verifier a bytes usando codificación US-ASCII</li>
     *   <li>Aplicación del hash SHA-256 sobre los bytes</li>
     *   <li>Codificación del hash en Base64 URL-safe sin padding</li>
     * </ul>
     *
     * <p>El code challenge resultante puede ser enviado de forma segura en URLs
     * y parámetros de consulta sin riesgo de comprometer la seguridad del flujo.</p>
     *
     * @param verifier el code verifier previamente generado que se utilizará para
     *                 crear el code challenge correspondiente
     *
     * @return String el code challenge codificado en Base64 URL-safe, derivado
     *         del verifier mediante SHA-256
     *
     * @throws NoSuchAlgorithmException si el algoritmo SHA-256 no está disponible
     *                                  en el entorno de ejecución
     *
     * @see #generateCodeVerifier()
     * @see MessageDigest
     * @since 1.0
     */
    public static String generateCodeChallenge(String verifier) throws NoSuchAlgorithmException {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(MessageDigest.getInstance("SHA-256")
                        .digest(verifier.getBytes(StandardCharsets.US_ASCII)));
    }
}
