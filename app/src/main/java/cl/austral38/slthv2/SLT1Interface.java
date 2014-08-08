package cl.austral38.slthv2;

public interface SLT1Interface {
	/**
	 inicia una medicion
	 **/
	public void start();
	/**
	 detiene una medicion
	 **/
	public void stop();
	/**
	 * Cierra la comunicacion con el dispositivo, limpia las variables
	 * **/
	public void close();

	/**
	 * Indica el estado del dispositivo conectado/desconectado
	 * **/
	public boolean isConnected();
	/**
	 * Obtiene el nombre del dispositivo
	 * */
	public String getDeviceTitle();
	/**
	 * configura el numero de muestras y el intervalo de medicion
	 * 
	 * */
	public void setConfiguration(int samples,int interval);

}
