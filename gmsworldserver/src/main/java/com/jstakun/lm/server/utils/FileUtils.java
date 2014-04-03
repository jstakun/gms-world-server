package com.jstakun.lm.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
//import com.google.appengine.api.files.AppEngineFile;
//import com.google.appengine.api.files.FileService;
//import com.google.appengine.api.files.FileServiceFactory;
//import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

public class FileUtils {

	/*public static BlobKey saveFile(String fileName, InputStream is) throws IOException {
		FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile file = fileService.createNewBlobFile("image/jpeg", fileName);
        FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);
        
        int nRead;
        byte[] data = new byte[8192];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            writeChannel.write(ByteBuffer.wrap(data, 0, nRead));
        }

        writeChannel.closeFinally();
        
        return fileService.getBlobKey(file);
	}*/	
	
	public static void saveFileV2(String fileName, InputStream is, double lat, double lng) throws IOException {
		String bucketName = AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName();
		GcsService gcsService = GcsServiceFactory.createGcsService();
        GcsFilename filename = new GcsFilename(bucketName, fileName);
        GcsFileOptions options = new GcsFileOptions.Builder()
            .mimeType("image/jpeg")
            .acl("public-read")
            .addUserMetadata("lat", Double.toString(lat))
            .addUserMetadata("lng", Double.toString(lng))
            .build();
        GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
        
        int nRead;
        byte[] data = new byte[8192];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            writeChannel.write(ByteBuffer.wrap(data, 0, nRead));
        }
        
        writeChannel.close();
	}
	
	public static boolean deleteFileV2(String fileName) throws IOException {
		String bucketName = AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName();
		GcsService gcsService = GcsServiceFactory.createGcsService();
        GcsFilename filename = new GcsFilename(bucketName, fileName);
        return gcsService.delete(filename);
	}
	
	public static String getImageUrl(BlobKey blobKey) {
		//This URL is served by a high-performance dynamic image serving infrastructure that is available globally. 
		//The URL returned by this method is always public, but not guessable; private URLs are not currently supported. 
		//If you wish to stop serving the URL, delete the underlying blob key. This takes up to 24 hours to take effect. 
		//The URL format also allows dynamic resizing and crop with certain restrictions. 
		//To get dynamic resizing and cropping simply append options to the end of the url obtained 
		//via this call. Here is an example: getServingUrl -> "http://lh3.ggpht.com/SomeCharactersGoesHere"
        //To get a 32 pixel sized version (aspect-ratio preserved) simply append "=s32" to the url: "http://lh3.ggpht.com/SomeCharactersGoesHere=s32"
        //To get a 32 pixel cropped version simply append "=s32-c": "http://lh3.ggpht.com/SomeCharactersGoesHere=s32-c"
        //Valid sizes are any integer in the range [0, 1600] (maximum is available as SERVING_SIZES_LIMIT).

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions sou = ServingUrlOptions.Builder.withBlobKey(blobKey);
        String imageUrl = imagesService.getServingUrl(sou);
        return imageUrl;
	}
	
	//"http://storage.googleapis.com/" + bucketName + "/" + fileName;
	public static String getImageUrlV2(String fileName) {
		String bucketName = AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName();
		BlobKey bk = getCloudStorageBlobKey(bucketName, fileName);
		return getImageUrl(bk);
	}
	
	private static BlobKey getCloudStorageBlobKey(String bucket_name, String object_name)
	{       
	    String cloudStorageURL = "/gs/" + bucket_name + "/" + object_name;
	    BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
	    BlobKey bk = bs.createGsBlobKey(cloudStorageURL);
	    return bk;
	} 
}
