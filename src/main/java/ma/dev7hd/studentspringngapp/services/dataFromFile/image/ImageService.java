package ma.dev7hd.studentspringngapp.services.dataFromFile.image;

import org.springframework.stereotype.Service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;

@Service
public class ImageService implements IImageService {
    @Override
    public byte[] resizeImageWithAspectRatio(byte[] originalImageData) throws IOException {
        // Convert byte[] to BufferedImage
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(originalImageData);
        BufferedImage originalImage = ImageIO.read(byteArrayInputStream);

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate the aspect ratio of the original image
        double aspectRatio = (double) originalWidth / originalHeight;

        int newWidth;
        int newHeight;

        // Adjust width and height based on the aspect ratio
        int maxHeight = 1000;
        int maxWidth = 1000;

        if (maxWidth / (double) maxHeight > aspectRatio) {
            // Width is too large, adjust it
            newWidth = (int) (maxHeight * aspectRatio);
            newHeight = maxHeight;
        } else {
            // Height is too large, adjust it
            newWidth = maxWidth;
            newHeight = (int) (maxWidth / aspectRatio);
        }

        // Resize the image while keeping the aspect ratio
        Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // Create a new BufferedImage without metadata
        BufferedImage resizedBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        // Draw the resized image into the new BufferedImage
        Graphics2D graphics = resizedBufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(resizedImage, 0, 0, null);
        graphics.dispose();

        // Convert the BufferedImage to a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedBufferedImage, "jpg", byteArrayOutputStream);  // You can choose the format: "jpg", "png", etc.
        return byteArrayOutputStream.toByteArray();
    }

}
