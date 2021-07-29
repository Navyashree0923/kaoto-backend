package io.zimara.backend.metadata.parser;

import io.zimara.backend.metadata.ParseCatalog;
import io.zimara.backend.model.Metadata;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.TagOpt;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class GithubParseCatalog<T extends Metadata> implements ParseCatalog<T> {

    Logger log = Logger.getLogger(GithubParseCatalog.class);

    private final CompletableFuture<List<T>> metadata = new CompletableFuture<>();

    public GithubParseCatalog(String url, String tag) {
        log.trace("Warming up kamelet catalog in " + url);
        metadata.completeAsync(() -> cloneRepoAndParse(url, tag));
    }

    private List<T> cloneRepoAndParse(String url, String tag) {
        List<T> metadataList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        final List<CompletableFuture<T>> futureMetadatas = Collections.synchronizedList(new CopyOnWriteArrayList<>());

        File file = null;
        try {
            log.trace("Creating temporary folder.");
            file = Files.createTempDirectory("kamelet-catalog").toFile();
            file.setExecutable(true, true);
            file.setReadable(true, true);
            file.setWritable(true, true);

            log.trace("Cloning git repository.");
            Git.cloneRepository()
                    .setCloneSubmodules(true)
                    .setURI(url)
                    .setDirectory(file)
                    .setBranch(tag)
                    .setTagOption(TagOpt.FETCH_TAGS)
                    .call();

        } catch (GitAPIException | IOException e) {
            log.error("Error trying to clone repository.", e);
        }

        try {
            log.trace("Parsing all kamelet files in the folder.");
            if(file != null) {
                Files.walkFileTree(file.getAbsoluteFile().toPath(), getFileVisitor(metadataList, futureMetadatas));
            }
            CompletableFuture.allOf(futureMetadatas.toArray(new CompletableFuture[0])).join();
        } catch (IOException | NullPointerException e) {
            log.error("Error trying to parse kamelet catalog.", e);
        }

        return metadataList;

    }

    @Override
    public CompletableFuture<List<T>> parse() {
        return metadata;
    }

    protected abstract YamlProcessFile<T> getFileVisitor(List<T> metadataList, List<CompletableFuture<T>> futureMetadata);

}
