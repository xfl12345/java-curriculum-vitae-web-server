package cc.xfl12345.person.cv.utility;

import cc.xfl12345.person.cv.json.PackageLandmark;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedHashMap;

public class ConsoleCharsetUtils {
    protected String microsoftWindowsCodePageJsonFileName = "microsoft_windows_code_page.json";

    protected LinkedHashMap<String, String> microsoftWindowsCodePageMap = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() throws Exception {
        URL localMapFileURL = PackageLandmark.class.getResource(microsoftWindowsCodePageJsonFileName);
        if (localMapFileURL != null) {
            Reader reader = new InputStreamReader(localMapFileURL.openStream(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            microsoftWindowsCodePageMap = (LinkedHashMap<String, String>) mapper.readValue(reader, LinkedHashMap.class);
            reader.close();
        } else {
            throw new FileNotFoundException(microsoftWindowsCodePageJsonFileName);
        }
    }

    /**
     * 尝试获取当前环境的编码。如果获取失败，则返回 null
     */
    public Charset getCharset() {
        Charset charset = null;
        switch (Platform.getOSType()) {
            case Platform.WINDOWS -> {
                if (Kernel32.INSTANCE.GetConsoleWindow() != null) {
                    String charsetName = microsoftWindowsCodePageMap.get(
                        "" + getWindowsShellCodePageNumber()
                    );
                    charset = getCharsetForName(charsetName);
                }
            }
            case Platform.LINUX -> {
                String currentLang = System.getenv("LANG");
                if (currentLang != null) {
                    int dotSplitCharacterIndex = currentLang.lastIndexOf('.');
                    if (dotSplitCharacterIndex > 0 && dotSplitCharacterIndex < currentLang.length() - 2) {
                        String charsetName = currentLang.substring(dotSplitCharacterIndex + 1);
                        charset = getCharsetForName(charsetName);
                    }
                }
            }
        }

        return charset;
    }

    protected int getWindowsShellCodePageNumber() {
        return Kernel32.INSTANCE.GetConsoleCP();
    }

    protected Charset getCharsetForName(String charsetName) {
        Charset charset = null;
        try {
            charset = Charset.forName(charsetName);
        } catch (UnsupportedCharsetException e) {
            System.err.println(ConsoleCharsetUtils.class.getCanonicalName() + ": Unsupported charset: " + e.getCharsetName());
        } catch (IllegalCharsetNameException e) {
            System.err.println(ConsoleCharsetUtils.class.getCanonicalName() + ": Illegal charset name: " + e.getCharsetName());
        } catch (Exception e) {
            // ignore
        }

        return charset;
    }
}
