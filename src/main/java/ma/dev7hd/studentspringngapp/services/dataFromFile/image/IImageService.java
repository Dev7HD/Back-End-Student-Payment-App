package ma.dev7hd.studentspringngapp.services.dataFromFile.image;

import java.io.IOException;

public interface IImageService {
    byte[] resizeImageWithAspectRatio(byte[] inputPhoto) throws IOException;
}
