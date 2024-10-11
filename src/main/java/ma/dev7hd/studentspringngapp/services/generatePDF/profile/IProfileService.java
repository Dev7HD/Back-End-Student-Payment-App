package ma.dev7hd.studentspringngapp.services.generatePDF.profile;

import ma.dev7hd.studentspringngapp.dtos.ProfileDTO;

import java.io.IOException;

public interface IProfileService {
    ProfileDTO generateProfile() throws IOException;
}
