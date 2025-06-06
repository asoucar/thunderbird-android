package com.fsck.k9.activity.loader;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.loader.content.AsyncTaskLoader;

import com.fsck.k9.helper.MimeTypeUtil;
import com.fsck.k9.message.Attachment.LoadingState;
import net.thunderbird.core.logging.legacy.Log;

import com.fsck.k9.activity.misc.Attachment;
import com.fsck.k9.mail.internet.MimeUtility;

/**
 * Loader to fetch metadata of an attachment.
 */
public class AttachmentInfoLoader  extends AsyncTaskLoader<Attachment> {
    private final Attachment sourceAttachment;
    private Attachment cachedResultAttachment;


    public AttachmentInfoLoader(Context context, Attachment attachment) {
        super(context);
        if (attachment.state != LoadingState.URI_ONLY) {
            throw new IllegalArgumentException("Attachment provided to metadata loader must be in URI_ONLY state");
        }

        sourceAttachment = attachment;
    }

    @Override
    protected void onStartLoading() {
        if (cachedResultAttachment != null) {
            deliverResult(cachedResultAttachment);
        }

        if (takeContentChanged() || cachedResultAttachment == null) {
            forceLoad();
        }
    }

    @Override
    public Attachment loadInBackground() {
        try {
            Uri uri = sourceAttachment.uri;
            String contentType = sourceAttachment.contentType;

            long size = -1;
            String name = null;

            ContentResolver contentResolver = getContext().getContentResolver();

            Cursor metadataCursor = contentResolver.query(
                    uri,
                    new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE },
                    null,
                    null,
                    null);

            if (metadataCursor != null) {
                try {
                    if (metadataCursor.moveToFirst()) {
                        name = metadataCursor.getString(0);
                        size = metadataCursor.getInt(1);
                    }
                } finally {
                    metadataCursor.close();
                }
            }

            if (name == null) {
                name = uri.getLastPathSegment();
            }

            String usableContentType = contentResolver.getType(uri);
            if (usableContentType == null && contentType != null && contentType.indexOf('*') != -1) {
                usableContentType = contentType;
            }

            if (usableContentType == null) {
                usableContentType = MimeTypeUtil.getMimeTypeByExtension(name);
            }

            if (!sourceAttachment.allowMessageType && MimeUtility.isMessageType(usableContentType)) {
                usableContentType = MimeTypeUtil.DEFAULT_ATTACHMENT_MIME_TYPE;
            }

            if (size <= 0) {
                String uriString = uri.toString();
                if (uriString.startsWith("file://")) {
                    File f = new File(uriString.substring("file://".length()));
                    size = f.length();
                } else {
                    Log.v("Not a file: %s", uriString);
                }
            } else {
                Log.v("old attachment.size: %d", size);
            }
            Log.v("new attachment.size: %d", size);

            cachedResultAttachment = sourceAttachment.deriveWithMetadataLoaded(usableContentType, name, size);
            return cachedResultAttachment;
        } catch (Exception e) {
            Log.e(e, "Error getting attachment meta data");

            cachedResultAttachment = sourceAttachment.deriveWithLoadCancelled();
            return cachedResultAttachment;
        }
    }
}
