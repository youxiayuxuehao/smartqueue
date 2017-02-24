package com.xuehao.smartqueue.uia;

import java.io.File;
import java.net.URL;
import java.util.List;


public interface ConfigLoader {

	List<ZkCluster> loadFromFile(String filePath);

	List<ZkCluster> loadFromFile(File file);
	
	List<ZkCluster> load(String path);
	
	List<ZkCluster> loadFromUrl(URL url);
}
