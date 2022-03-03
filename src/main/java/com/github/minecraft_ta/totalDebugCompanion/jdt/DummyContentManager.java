package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.google.common.collect.Maps;
import org.eclipse.core.internal.content.ContentType;
import org.eclipse.core.internal.content.ContentTypeCatalog;
import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.runtime.content.IContentType;

class DummyContentManager extends ContentTypeManager {

    ContentType boi;

    @Override
    public IContentType[] getAllContentTypes() {
        return new IContentType[]{getJava()};
    }

    @Override
    public IContentType getContentType(String contentTypeIdentifier) {
        return getJava();
    }

    private ContentType getJava() {
        if (boi == null)
            boi = ContentType.createContentType(new ContentTypeCatalog(this, 0), "1", "java", (byte) 1, new String[]{"java"}
                    , new String[0], new String[0], "adfw", "ad", Maps.newHashMap(), null);
        return boi;
    }
}
