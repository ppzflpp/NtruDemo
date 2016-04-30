/**
 * Copyright 2007 The Apache Software Foundation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dragon.ntrudemo.wordCheck.knife;

import android.provider.SyncStateContract;

import com.dragon.ntrudemo.wordCheck.Constants.Constants;
import com.dragon.ntrudemo.wordCheck.exception.PaodingAnalysisException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * @since 2.0.0
 */
public class PaodingMaker {

    public PaodingMaker() {
    }

    private static ObjectHolder<Properties> propertiesHolder = new ObjectHolder<Properties>();
    private static ObjectHolder<Paoding> paodingHolder = new ObjectHolder<Paoding>();

    public static Paoding make() {
        //postPropertiesLoaded(p);
        return implMake();
    }

    // -------------------私有 或 辅助方法----------------------------------

	/*private static boolean modified(Properties p) throws IOException {
        String lastModifieds = p
				.getProperty("paoding.analysis.properties.lastModifieds");
		String[] lastModifedsArray = lastModifieds.split(";");
		String files = p.getProperty("paoding.analysis.properties.files");
		String[] filesArray = files.split(";");
		for (int i = 0; i < filesArray.length; i++) {
			File file = getFile(filesArray[i]);
			if (file.exists()
					&& !String.valueOf(getFileLastModified(file)).equals(
							lastModifedsArray[i])) {
				return true;
			}
		}
		return false;
	}*/

    private static long getFileLastModified(File file) throws IOException {
        String path = file.getPath();
        int jarIndex = path.indexOf(".jar!");
        if (jarIndex == -1) {
            return file.lastModified();
        } else {
            path = path.replaceAll("%20", " ").replaceAll("\\\\", "/");
            jarIndex = path.indexOf(".jar!");
            int protocalIndex = path.indexOf(":");
            String jarPath = path.substring(protocalIndex + ":".length(),
                    jarIndex + ".jar".length());
            File jarPathFile = new File(jarPath);
            JarFile jarFile;
            try {
                jarFile = new JarFile(jarPathFile);
                String entryPath = path.substring(jarIndex + ".jar!/".length());
                JarEntry entry = jarFile.getJarEntry(entryPath);
                return entry.getTime();
            } catch (IOException e) {
                System.err.println("error in handler path=" + path);
                System.err.println("error in handler jarPath=" + jarPath);
                throw e;
            }
        }
    }

	/*private static String getDicHome(Properties p) {
        setDicHomeProperties(p);
		return p.getProperty("paoding.dic.home.absolute.path");
	}

	private static void postPropertiesLoaded(Properties p) {
		if ("done".equals(p
				.getProperty("paoding.analysis.postPropertiesLoaded"))) {
			return;
		}
		setDicHomeProperties(p);
		p.setProperty("paoding.analysis.postPropertiesLoaded", "done");
	}*/

    private static Paoding implMake() {

        // 将要返回的Paoding对象，它可能是新创建的，也可能使用paodingHolder中已有的Paoding对象
        Paoding paoding;

        // 作为本次返回的Paoding对象在paodingHolder中的key，使之后同样的key不会重复创建Paoding对象
        final Object paodingKey;
        paodingKey = "exist";
        paoding = (Paoding) paodingHolder.get(paodingKey);
        if (paoding != null) {
            return paoding;
        }
        try {
            paoding = createPaodingWithKnives();
            final Paoding finalPaoding = paoding;

            new Function() {
                public void run() throws Exception {
                    // 编译词典-对词典进行可能的处理，以符合分词器的要求
                    /*if (compiler.shouldCompile(p)) {
						Dictionaries dictionaries = readUnCompiledDictionaries(p);
						Paoding tempPaoding = createPaodingWithKnives();
						setDictionaries(tempPaoding, dictionaries);
						compiler.compile(dictionaries, tempPaoding);
					}*/

                    // 使用编译后的词典
                    final Dictionaries dictionaries = readCompliedDictionaries();
                    System.out.println("read");
                    System.gc();
                    setDictionaries(finalPaoding, dictionaries);
                    System.out.println("set");

                    // 启动字典动态转载/卸载检测器
                    // 侦测时间间隔(秒)。默认为60秒。如果设置为０或负数则表示不需要进行检测
					/*int interval = 60;
					if (interval > 0) {
						dictionaries.startDetecting(interval,
								new DifferenceListener() {
									public void on(Difference diff)
											throws Exception {
										dictionaries.stopDetecting();
										// 此处调用run方法，以当检测到**编译后**的词典变更/删除/增加时，
										// 重新编译源词典、重新创建并启动dictionaries自检测
										run();
									}
								});
					}*/
                }
            }.run();
            // Paoding对象创建成功！此时可以将它寄放到paodingHolder中，给下次重复利用
            paodingHolder.set(paodingKey, paoding);

            return paoding;
        } catch (Exception e) {
            throw new PaodingAnalysisException("", e);
        }
    }

    private static Paoding createPaodingWithKnives(/*Properties p*/)
            throws Exception {
        // 如果PaodingHolder中并没有缓存该属性文件或对象对应的Paoding对象，
        // 则根据给定的属性创建一个新的Paoding对象，并在返回之前存入paodingHolder
        Paoding paoding = new Paoding();

        // 寻找传说中的Knife。。。。
        final String[] KnifeKey = new String[3];
        final String[] KnifeValue = new String[3];
        KnifeKey[0] = "paoding.knife.class.letterKnife";
        KnifeValue[0] = "com.dragon.ntrudemo.wordCheck.knife.LetterKnife";
        KnifeKey[1] = "paoding.knife.class.numberKnife";
        KnifeValue[1] = "com.dragon.ntrudemo.wordCheck.knife.NumberKnife";
        KnifeKey[2] = "paoding.knife.class.cjkKnife";
        KnifeValue[2] = "com.dragon.ntrudemo.wordCheck.knife.CJKKnife";
        final Map<String, Knife> knifeMap = new HashMap<String, Knife>();
        final List<Knife> knifeList = new LinkedList<Knife>();
        final List<Function> functions = new LinkedList<Function>();
        for (int i = 0; i < 3; i++) {
            //Map.Entry e = (Map.Entry) iter.next();
            final String key = (String) KnifeKey[i];
            final String value = (String) KnifeValue[i];
            //int index = key.indexOf(Constants.KNIFE_CLASS);
            //if (index == 0 && key.length() > Constants.KNIFE_CLASS.length()) {
            final int end = key
                    .indexOf('.', Constants.KNIFE_CLASS.length());
            if (end == -1) {
                Class clazz = Class.forName(value);
                Knife knife = (Knife) clazz.newInstance();
                knifeList.add(knife);
                knifeMap.put(key, knife);
                //log.info("add knike: " + value);
            } else {
                // 由于属性对象属于hash表，key的读取顺序不和文件的顺序一致，不能保证属性设置时，knife对象已经创建
                // 所以这里只定义函数放到functions中，待到所有的knife都创建之后，在执行该程序
                functions.add(new Function() {
                    public void run() throws Exception {
                        String knifeName = key.substring(0, end);
                        Object obj = knifeMap.get(knifeName);
                        if (!obj
                                .getClass()
                                .getName()
                                .equals(
                                        "org.springframework.beans.BeanWrapperImpl")) {
                            Class beanWrapperImplClass = Class
                                    .forName("org.springframework.beans.BeanWrapperImpl");
                            Method setWrappedInstance = beanWrapperImplClass
                                    .getMethod("setWrappedInstance",
                                            new Class[]{Object.class});
                            Object beanWrapperImpl = beanWrapperImplClass
                                    .newInstance();
                            setWrappedInstance.invoke(beanWrapperImpl,
                                    new Object[]{obj});
                            knifeMap.put(knifeName, (Knife) beanWrapperImpl);
                            obj = beanWrapperImpl;
                        }
                        String propertyName = key.substring(end + 1);
                        Method setPropertyValue = obj.getClass().getMethod(
                                "setPropertyValue",
                                new Class[]{String.class, Object.class});
                        setPropertyValue.invoke(obj, new Object[]{
                                propertyName, value});
                    }
                });
            }
        }


        // 完成所有留后执行的程序
        for (Iterator iterator = functions.iterator(); iterator.hasNext(); ) {
            Function function = (Function) iterator.next();
            function.run();
        }
        // 把刀交给庖丁
        paoding.setKnives(knifeList);
        return paoding;
    }

    private static void setDictionaries(Paoding paoding,
                                        Dictionaries dictionaries) {
        Knife[] knives = paoding.getKnives();
        for (int i = 0; i < knives.length; i++) {
            Knife knife = (Knife) knives[i];
            if (knife instanceof DictionariesWare) {
                System.out.println(knife.getClass().getName());
                ((DictionariesWare) knife).setDictionaries(dictionaries);
            }
        }
    }

    private static File getFile(String path) {
        File file;
        URL url;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
            url = getClassLoader().getResource(path);
            final boolean fileExist = url != null;
            file = new File(fileExist ? url.getFile() : path) {
                private static final long serialVersionUID = 4009013298629147887L;

                public boolean exists() {
                    return fileExist;
                }
            };
        } else {
            file = new File(path);
        }
        return file;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = PaodingMaker.class.getClassLoader();
        }
        return loader;
    }

    // --------------------------------------------------------------------

    private static class ObjectHolder<T> {

        private ObjectHolder() {
        }

        private Map<Object, T> objects = new HashMap<Object, T>();

        public T get(Object name) {
            return objects.get(name);
        }

        public void set(Object name, T object) {
            objects.put(name, object);
        }

        public void remove(Object name) {
            objects.remove(name);
        }
    }

    private static interface Function {
        public void run() throws Exception;
    }

    private static String getSystemEnv(String name) {
        try {
            return System.getenv(name);
        } catch (Error error) {
            String osName = System.getProperty("os.name").toLowerCase();
            try {
                String cmd;
                if (osName.indexOf("win") != -1) {
                    cmd = "cmd /c SET";
                } else {
                    cmd = "/usr/bin/printenv";
                }
                Process process = Runtime.getRuntime().exec(cmd);
                InputStreamReader isr = new InputStreamReader(process.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null && line.startsWith(name)) {
                    int index = line.indexOf(name + "=");
                    if (index != -1) {
                        return line.substring(index + name.length() + 1);
                    }
                }
            } catch (Exception e) {
                //log.warn("unable to read env from os．" + e.getMessage(), e);
            }
        }
        return null;
    }

    protected static Dictionaries readCompliedDictionaries() {
        String dicHome = "/data/data/mars.activity05/files";
        String noiseCharactor = "x-noise-charactor";
        String noiseWord = "x-noise-word";
        String unit = "x-unit";
        String confucianFamilyName = "x-confucian-family-name";
        String combinatorics = "x-for-combinatorics";
        String charsetName = "UTF-8";
        return new CompiledFileDictionaries(
                dicHome, noiseCharactor, noiseWord, unit,
                confucianFamilyName, combinatorics, charsetName);
    }

}
