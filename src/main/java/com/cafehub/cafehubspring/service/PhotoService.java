package com.cafehub.cafehubspring.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cafehub.cafehubspring.domain.Cafe;
import com.cafehub.cafehubspring.domain.Photo;
import com.cafehub.cafehubspring.exception.http.InternalServerErrorException;
import com.cafehub.cafehubspring.exception.http.NotFoundException;
import com.cafehub.cafehubspring.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoService {

    @Value("${s3.bucketName}")
    private String bucketName;

    private final PhotoRepository photoRepository;

    private final AmazonS3Client amazonS3Client;

    /**
     * Photo 저장 |
     * 사진을 S3에 저장한 후에 디비에 관련 정보를 입력한다. 저장 중 오류가 발생하면 500(Internal Server Error)을 던진다.
     */
    @Transactional
    public Photo save(Cafe cafe, String fileName, MultipartFile file) {
        log.info("IN PROGRESS | Photo 저장 At " + LocalDateTime.now() +
                " | 카페 아이디 = "  + cafe.getId() + " 파일명 = " + fileName);
        String folderName = cafe.getCafeName() + "-" + cafe.getId();
        String url = insertFileToS3(folderName, fileName, file);
        try {
            Photo photo = Photo.builder().fileName(fileName).url(url).cafe(cafe).build();
            Photo savedPhoto = photoRepository.save(photo);
            log.info("COMPLETE | Photo 저장 At " + LocalDateTime.now() + " | " + savedPhoto);
            return savedPhoto;
        } catch (Exception e) {
            throw new InternalServerErrorException("Photo save 중 에러 발생", e);
        }
    }

    /**
     * Photo 주입 |
     * 사진을 S3에 주입한 후에 디비에 관련 정보를 입력한다. 저장 중 오류가 발생하면 500(Internal Server Error)을 던진다.
     */
    @Transactional
    public Photo inject(Cafe cafe, String fileName, File file) {
        log.info("IN PROGRESS | Photo 주입 At " + LocalDateTime.now() +
                " | 카페 아이디 = "  + cafe.getId() + " 파일명 = " + fileName);
        String folderName = cafe.getCafeName() + "-" + cafe.getId();
        String url = injectFileToS3(folderName, fileName, file);
        try {
            Photo photo = Photo.builder().fileName(fileName).url(url).cafe(cafe).build();
            Photo injectedPhoto = photoRepository.save(photo);
            log.info("COMPLETE | Photo 저장 At " + LocalDateTime.now() + " | " + injectedPhoto.getId());
            return injectedPhoto;
        } catch (Exception e) {
            throw new InternalServerErrorException("Photo 주입 중 에러 발생", e);
        }
    }

    /**
     * S3에 파일 저장 |
     * 파일을 전환하고 특정 파일 관련된 폴더에 파일을 저장하고 URL을 반환한다. 파일 전환시 오류가 발생하면 500(Internal Server Error)을 던진다.
     */
    private String insertFileToS3(String folderName, String fileName, MultipartFile file) {
        log.info("IN PROGRESS | S3에 파일 저장 At " + LocalDateTime.now() +
                " | 폴더명 = " + folderName + " | 파일명 = " + fileName);
        try {
            File convertedFile = convertMultiPartToFile(file);
            String uploadingFileName = folderName + "/" + fileName;

            amazonS3Client.putObject(new PutObjectRequest(bucketName, uploadingFileName, convertedFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            convertedFile.delete();
            String url = amazonS3Client.getUrl(bucketName, uploadingFileName).toString();
            log.info("COMPLETE | S3에 파일 저장 At " + LocalDateTime.now() + " | " + url);
            return url;
        } catch (IOException e) {
            throw new InternalServerErrorException("Photo insertFileToS3 중 에러 발생", e);
        }
    }

    /**
     * S3에 파일 주입 |
     * 특정 파일 관련된 폴더에 파일을 주입하고 URL을 반환한다.
     */
    private String injectFileToS3(String folderName, String fileName, File file) {
        log.info("IN PROGRESS | S3에 파일 주입 At " + LocalDateTime.now() +
                " | 폴더명 = " + folderName + " | 파일명 = " + fileName);

        try {
            String uploadingFileName = folderName + "/" + fileName;

            amazonS3Client.putObject(new PutObjectRequest(bucketName, uploadingFileName, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            String url = amazonS3Client.getUrl(bucketName, uploadingFileName).toString();
            log.info("COMPLETE | S3에 파일 주입 At " + LocalDateTime.now() + " | " + url);
            return url;
        } catch (Exception e) {
            throw new InternalServerErrorException("Photo injectFileToS3 중 에러 발생", e);
        }
    }

    /**
     * MultipartFile을 File로 전환 |
     * MultipartFile을 받아서 File의 형태로 전환하여 반환한다.
     */
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        log.info("IN PROGRESS | MultipartFile을 File로 전환 At " + LocalDateTime.now());
        File convertingFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(convertingFile);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        log.info("COMPLETE | MultipartFile을 File로 전환 At " + LocalDateTime.now());
        return convertingFile;
    }

    /**
     * Photo 삭제 |
     * 사진을 S3에서 삭제한 후에 디비에서 관련 정보 찾아 삭제를 한다. 디비에서 관련 정보를 찾는 중 오류가 발생하면 404(Not Found)를 던지고, 디비에서
     * 데이터를 삭제하는 과정에서 오류가 발생하면 500(Internal Server Error)을 던진다.
     */
    @Transactional
    public void delete(Cafe cafe, String fileName) {
        log.info("IN PROGRESS | Photo 삭제 At " + LocalDateTime.now());
        String folderName = cafe.getCafeName() + "-" + cafe.getId();
        deleteFileFromS3(folderName, fileName);
        Optional<Photo> foundPhoto = photoRepository.findByFileName(fileName);
        if (foundPhoto.isEmpty()) {
            throw new NotFoundException("사진이 존재하지 않습니다");
        }
        try {
            photoRepository.delete(foundPhoto.get());
        } catch (Exception e) {
            throw new InternalServerErrorException("Photo delete 중 에러 발생", e);
        }
    }

    /**
     * S3에 파일 삭제 |
     * S3로부터 파일을 삭제한다. 삭제 중 오류가 발생하면 500(Internal Server Error)을 던진다.
     */
    private void deleteFileFromS3(String folderName, String fileName) {
        String deletingFileName = folderName + "/" + fileName;
        try {
            amazonS3Client.deleteObject(bucketName, deletingFileName);
        } catch (Exception e) {
            throw new InternalServerErrorException("Photo deleteFileFromS3 중 에러 발생", e);
        }
    }

}