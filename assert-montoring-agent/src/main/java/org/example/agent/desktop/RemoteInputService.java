package org.example.agent.desktop;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Executes remote input actions (mouse movement, clicks, scrolling, key presses,
 * clipboard synchronization, file transfers) on the employee machine using Java Robot.
 */
public class RemoteInputService {

    private final Robot robot;
    private final Dimension screenSize;

    // Track active file transfers
    private final Map<String, FileOutputStream> openFiles = new ConcurrentHashMap<>();

    public RemoteInputService() throws AWTException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new AWTException("GraphicsEnvironment is headless; cannot simulate inputs.");
        }
        this.robot = new Robot();
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    }

    public void handleInput(String inputType, Double normX, Double normY, Integer button, Integer deltaY, Integer keyCode, String key) {
        if (inputType == null) return;

        try {
            switch (inputType.toUpperCase()) {
                case "MOUSE_MOVE":
                    if (normX != null && normY != null) {
                        int x = (int) Math.round(normX * screenSize.width);
                        int y = (int) Math.round(normY * screenSize.height);
                        robot.mouseMove(x, y);
                    }
                    break;

                case "MOUSE_DOWN":
                    robot.mousePress(getButtonMask(button));
                    break;

                case "MOUSE_UP":
                    robot.mouseRelease(getButtonMask(button));
                    break;

                case "CLICK":
                    int clickMask = getButtonMask(button);
                    robot.mousePress(clickMask);
                    robot.delay(15);
                    robot.mouseRelease(clickMask);
                    break;

                case "DOUBLE_CLICK":
                    int dblMask = getButtonMask(button);
                    robot.mousePress(dblMask);
                    robot.delay(15);
                    robot.mouseRelease(dblMask);
                    robot.delay(50);
                    robot.mousePress(dblMask);
                    robot.delay(15);
                    robot.mouseRelease(dblMask);
                    break;

                case "SCROLL":
                    if (deltaY != null && deltaY != 0) {
                        robot.mouseWheel(deltaY > 0 ? 1 : -1);
                    }
                    break;

                case "KEY_DOWN":
                    if (keyCode != null && keyCode > 0) {
                        int awtKey = mapToAwtKey(keyCode, key);
                        if (awtKey != KeyEvent.VK_UNDEFINED) {
                            robot.keyPress(awtKey);
                        }
                    }
                    break;

                case "KEY_UP":
                    if (keyCode != null && keyCode > 0) {
                        int awtKey = mapToAwtKey(keyCode, key);
                        if (awtKey != KeyEvent.VK_UNDEFINED) {
                            robot.keyRelease(awtKey);
                        }
                    }
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("[RemoteInput] Error executing input action '" + inputType + "': " + e.getMessage());
        }
    }

    public void setClipboardText(String text) {
        if (text == null) return;
        try {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            System.out.println("[RemoteInput] Clipboard synchronized from admin.");
        } catch (Exception e) {
            System.err.println("[RemoteInput] Failed to set clipboard: " + e.getMessage());
        }
    }

    public void handleFileTransferMeta(String fileName, Long fileSize) {
        if (fileName == null || fileName.isBlank()) return;
        try {
            File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
            File targetFolder = new File(downloadsDir, "AssetManagement_Transfers");
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File outFile = new File(targetFolder, new File(fileName).getName());
            FileOutputStream fos = new FileOutputStream(outFile);
            openFiles.put(fileName, fos);
            System.out.println("[RemoteInput] Receiving file transfer: " + outFile.getAbsolutePath() + " (" + fileSize + " bytes)");
        } catch (Exception e) {
            System.err.println("[RemoteInput] Failed to start file receive: " + e.getMessage());
        }
    }

    public void handleFileTransferChunk(String fileName, String base64Data, boolean isLast) {
        FileOutputStream fos = openFiles.get(fileName);
        if (fos == null) return;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64Data);
            fos.write(bytes);
            if (isLast) {
                fos.flush();
                fos.close();
                openFiles.remove(fileName);
                System.out.println("[RemoteInput] Completed file transfer: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("[RemoteInput] Failed to write file chunk: " + e.getMessage());
        }
    }

    private int getButtonMask(Integer button) {
        if (button == null) return InputEvent.BUTTON1_DOWN_MASK;
        return switch (button) {
            case 1 -> InputEvent.BUTTON2_DOWN_MASK; // middle
            case 2 -> InputEvent.BUTTON3_DOWN_MASK; // right
            default -> InputEvent.BUTTON1_DOWN_MASK; // left
        };
    }

    private int mapToAwtKey(int jsKeyCode, String key) {
        return switch (jsKeyCode) {
            case 8 -> KeyEvent.VK_BACK_SPACE;
            case 9 -> KeyEvent.VK_TAB;
            case 13 -> KeyEvent.VK_ENTER;
            case 16 -> KeyEvent.VK_SHIFT;
            case 17 -> KeyEvent.VK_CONTROL;
            case 18 -> KeyEvent.VK_ALT;
            case 20 -> KeyEvent.VK_CAPS_LOCK;
            case 27 -> KeyEvent.VK_ESCAPE;
            case 32 -> KeyEvent.VK_SPACE;
            case 33 -> KeyEvent.VK_PAGE_UP;
            case 34 -> KeyEvent.VK_PAGE_DOWN;
            case 35 -> KeyEvent.VK_END;
            case 36 -> KeyEvent.VK_HOME;
            case 37 -> KeyEvent.VK_LEFT;
            case 38 -> KeyEvent.VK_UP;
            case 39 -> KeyEvent.VK_RIGHT;
            case 40 -> KeyEvent.VK_DOWN;
            case 45 -> KeyEvent.VK_INSERT;
            case 46 -> KeyEvent.VK_DELETE;
            case 91, 92 -> KeyEvent.VK_WINDOWS;
            default -> {
                if (jsKeyCode >= 65 && jsKeyCode <= 90) { // A-Z
                    yield jsKeyCode;
                }
                if (jsKeyCode >= 48 && jsKeyCode <= 57) { // 0-9
                    yield jsKeyCode;
                }
                if (jsKeyCode >= 112 && jsKeyCode <= 123) { // F1-F12
                    yield KeyEvent.VK_F1 + (jsKeyCode - 112);
                }
                yield KeyEvent.VK_UNDEFINED;
            }
        };
    }
}
