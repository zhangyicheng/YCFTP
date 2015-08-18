package com.galaxywind.ycftp.server;

import java.nio.channels.SelectionKey;

public interface NioHandler {
	 void execute(SelectionKey key);  
}
