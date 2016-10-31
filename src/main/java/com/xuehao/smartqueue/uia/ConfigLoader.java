package com.xuehao.smartqueue.uia;

import java.io.File;
import java.util.List;

public interface ConfigLoader {

	List<ZkCluster> loadFromFile(String filePath);

	List<ZkCluster> loadFromFile(File file);
}
