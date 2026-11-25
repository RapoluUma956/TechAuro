package org.example.agent.desktop;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Captures the employee's screen using Java Robot, downscales to standard resolution
 * preserving aspect ratio, and applies high-efficiency JPEG compression (quality 0.55).
 * Generates ~30-60 KB frames at 12-15 FPS, preventing buffer overflow (Error 1009)
 * while maintaining crisp readability for remote IT administration.
 */
public class ScreenCaptureService {

    private final Robot robot;
    private final Dimension screenSize;
    private final Rectangle screenRect;
    private final int targetWidth;
    private final int targetHeight;
    private final float compressionQuality;

    private final AtomicBoolean isStreaming = new AtomicBoolean(false);
    private Thread captureThread;

    public ScreenCaptureService() throws AWTException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new AWTException("GraphicsEnvironment is headless; cannot capture screen.");
        }
        this.robot = new Robot();
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenRect = new Rectangle(screenSize);

        // Calculate downscaled dimensions preserving aspect ratio (max width 1366)
        if (screenSize.width > 1366) {
            double scale = 1366.0 / screenSize.width;
            this.targetWidth = 1366;
            this.targetHeight = (int) Math.round(screenSize.height * scale);
        } else {
            this.targetWidth = screenSize.width;
            this.targetHeight = screenSize.height;
        }
        this.compressionQuality = 0.55f;
    }

    public int getScreenWidth() {
        return screenSize.width;
    }

    public int getScreenHeight() {
        return screenSize.height;
    }

    public synchronized void start(Consumer<ByteBuffer> frameConsumer) {
        if (isStreaming.get()) {
            return;
        }
        isStreaming.set(true);

        captureThread = new Thread(() -> {
            long targetFrameIntervalMs = 70; // ~14 FPS

            while (isStreaming.get()) {
                long start = System.currentTimeMillis();
                try {
                    ByteBuffer frameBuffer = captureFrame();
                    if (frameBuffer != null && isStreaming.get()) {
                        frameConsumer.accept(frameBuffer);
                    }
                } catch (Exception e) {
                    System.err.println("[ScreenCapture] Error capturing frame: " + e.getMessage());
                }

                long elapsed = System.currentTimeMillis() - start;
                long sleepTime = targetFrameIntervalMs - elapsed;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "ScreenCapture-Thread");

        captureThread.setDaemon(true);
        captureThread.start();
    }

    public synchronized void stop() {
        isStreaming.set(false);
        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
        }
    }

    public boolean isStreaming() {
        return isStreaming.get();
    }

    /**
     * Captures a single frame, rescales if necessary, and compresses as JPEG.
     */
    public ByteBuffer captureFrame() throws Exception {
        BufferedImage rawImage = robot.createScreenCapture(screenRect);
        if (rawImage == null) return null;

        BufferedImage finalImage;
        if (rawImage.getWidth() == targetWidth && rawImage.getHeight() == targetHeight) {
            finalImage = rawImage;
        } else {
            finalImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = finalImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2d.drawImage(rawImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 1024);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(finalImage, "jpg", baos);
            return ByteBuffer.wrap(baos.toByteArray());
        }

        ImageWriter writer = writers.next();
        try (MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos)) {
            writer.setOutput(mcios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(compressionQuality);
            }
            writer.write(null, new IIOImage(finalImage, null, null), param);
        } finally {
            writer.dispose();
        }

        return ByteBuffer.wrap(baos.toByteArray());
    }
}
