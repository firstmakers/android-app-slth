package cl.austral38.slthv2.models;

public class MessageTxt {
	public String[] message;
	public MessageTxt(int msg) {
		if(msg == 1)
			message = new String[]{"1","Se ha desconectado el sensor externo"};
		if(msg == 2)
			message = new String[]{"2", "Se ha desconectado el dispositivo"};
        if(msg == 3)
            message = new String[]{"3", "Lectura Finalizada"};
	}
}
