package com.kneelawk.commonevents.test.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;
import com.kneelawk.commonevents.impl.Platform;

public class TestPlatformImpl implements Platform {
    private final String modVersion;

    public TestPlatformImpl() {
        String modVersion;
        try (InputStream is = TestPlatformImpl.class.getClassLoader()
            .getResourceAsStream("META-INF/common-events-info.properties")) {
            Properties props = new Properties();
            props.load(is);
            modVersion = props.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException("Error reading common events own info file.", e);
        }
        this.modVersion = modVersion;
    }

    @Override
    public boolean isPhysicalClient() {
        return true;
    }

    @Override
    public String getModVersion() {
        return modVersion;
    }

    @Override
    public List<? extends ModFileHolder> getModFiles() {
        // gather all jars and dirs on the classpath
        List<ModFileHolder> collected = new ArrayList<>();
        // resource dirs are dirs on the classpath that contain a common-events.json file
        List<TestDirModFileHolder> resourceDirs = new ArrayList<>();
        List<Path> classDirs = new ArrayList<>();
        String[] classpathEntries = System.getProperty("java.class.path").split(Pattern.quote(File.pathSeparator));
        for (String classpathEntry : classpathEntries) {
            Path classpathPath = Path.of(classpathEntry);
            if (Files.exists(classpathPath)) {
                if (Files.isDirectory(classpathPath)) {
                    if (Files.exists(classpathPath.resolve("common-events.json"))) {
                        resourceDirs.add(new TestDirModFileHolder(classpathPath));
                    } else {
                        classDirs.add(classpathPath);
                    }
                } else if (classpathPath.endsWith(".jar")) {
                    collected.add(new TestJarModFileHolder(classpathPath));
                }
            }
        }

        if (resourceDirs.isEmpty()) return collected;

        // find the resource dir closest to each class dir and add the class dir to that resource dir's associated mod
        for (Path classDir : classDirs) {
            TestDirModFileHolder best = null;
            int minDistance = Integer.MAX_VALUE;
            for (TestDirModFileHolder holder : resourceDirs) {
                int distance = getDistance(classDir, holder.getResourcePath());
                if (distance < minDistance) {
                    best = holder;
                    minDistance = distance;
                }
            }

            assert best != null;
            best.addRootPath(classDir);
        }

        collected.addAll(resourceDirs);

        return collected;
    }

    private static int getCommonParent(Path a, Path b) {
        int endIndex = 0;
        Iterator<Path> aIter = a.iterator();
        Iterator<Path> bIter = b.iterator();
        while (aIter.hasNext() && bIter.hasNext()) {
            Path aSub = aIter.next();
            Path bSub = bIter.next();

            if (aSub.equals(bSub)) {
                endIndex++;
            } else {
                return endIndex;
            }
        }
        return endIndex;
    }

    private static int getDistance(Path a, Path b) {
        int common = getCommonParent(a, b);
        return a.getNameCount() + b.getNameCount() - common * 2;
    }

    @Override
    public Path getGameDirectory() {
        return Paths.get("").toAbsolutePath();
    }
}
