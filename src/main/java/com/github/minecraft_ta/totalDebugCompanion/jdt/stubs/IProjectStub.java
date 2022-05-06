package com.github.minecraft_ta.totalDebugCompanion.jdt.stubs;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import java.net.URI;
import java.util.Map;

public interface IProjectStub extends IProject {

    @Override
    default void build(int kind, String builderName, Map<String, String> args, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void build(int kind, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void build(IBuildConfiguration config, int kind, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void close(IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void create(IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default IBuildConfiguration getActiveBuildConfig() throws CoreException {
        return null;
    }

    @Override
    default IBuildConfiguration getBuildConfig(String configName) throws CoreException {
        return null;
    }

    @Override
    default IBuildConfiguration[] getBuildConfigs() throws CoreException {
        return new IBuildConfiguration[0];
    }

    @Override
    default IContentTypeMatcher getContentTypeMatcher() throws CoreException {
        return null;
    }

    @Override
    default IProjectDescription getDescription() throws CoreException {
        return null;
    }

    @Override
    default IFile getFile(String name) {
        return null;
    }

    @Override
    default IFolder getFolder(String name) {
        return null;
    }

    @Override
    default IProjectNature getNature(String natureId) throws CoreException {
        return null;
    }

    @Override
    default IPath getWorkingLocation(String id) {
        return null;
    }

    @Override
    default IProject[] getReferencedProjects() throws CoreException {
        return new IProject[0];
    }

    @Override
    default void clearCachedDynamicReferences() {

    }

    @Override
    default IProject[] getReferencingProjects() {
        return new IProject[0];
    }

    @Override
    default IBuildConfiguration[] getReferencedBuildConfigs(String configName, boolean includeMissing) throws CoreException {
        return new IBuildConfiguration[0];
    }

    @Override
    default boolean hasBuildConfig(String configName) throws CoreException {
        return false;
    }

    @Override
    default boolean hasNature(String natureId) throws CoreException {
        return false;
    }

    @Override
    default boolean isNatureEnabled(String natureId) throws CoreException {
        return false;
    }

    @Override
    default boolean isOpen() {
        return false;
    }

    @Override
    default void loadSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void move(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void open(int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void open(IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void saveSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void setDescription(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default boolean exists(IPath path) {
        return false;
    }

    @Override
    default IResource findMember(String path) {
        return null;
    }

    @Override
    default IResource findMember(String path, boolean includePhantoms) {
        return null;
    }

    @Override
    default IResource findMember(IPath path) {
        return null;
    }

    @Override
    default IResource findMember(IPath path, boolean includePhantoms) {
        return null;
    }

    @Override
    default String getDefaultCharset() throws CoreException {
        return null;
    }

    @Override
    default String getDefaultCharset(boolean checkImplicit) throws CoreException {
        return null;
    }

    @Override
    default IFile getFile(IPath path) {
        return null;
    }

    @Override
    default IFolder getFolder(IPath path) {
        return null;
    }

    @Override
    default IResource[] members() throws CoreException {
        return new IResource[0];
    }

    @Override
    default IResource[] members(boolean includePhantoms) throws CoreException {
        return new IResource[0];
    }

    @Override
    default IResource[] members(int memberFlags) throws CoreException {
        return new IResource[0];
    }

    @Override
    default IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException {
        return new IFile[0];
    }

    @Override
    default void setDefaultCharset(String charset) throws CoreException {

    }

    @Override
    default void setDefaultCharset(String charset, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default IResourceFilterDescription createFilter(int type, FileInfoMatcherDescription matcherDescription, int updateFlags, IProgressMonitor monitor) throws CoreException {
        return null;
    }

    @Override
    default IResourceFilterDescription[] getFilters() throws CoreException {
        return new IResourceFilterDescription[0];
    }

    @Override
    default void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {

    }

    @Override
    default void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {

    }

    @Override
    default void accept(IResourceVisitor visitor) throws CoreException {

    }

    @Override
    default void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {

    }

    @Override
    default void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {

    }

    @Override
    default void clearHistory(IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default IMarker createMarker(String type) throws CoreException {
        return null;
    }

    @Override
    default IResourceProxy createProxy() {
        return null;
    }

    @Override
    default void delete(boolean force, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {

    }

    @Override
    default boolean exists() {
        return false;
    }

    @Override
    default IMarker findMarker(long id) throws CoreException {
        return null;
    }

    @Override
    default IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
        return new IMarker[0];
    }

    @Override
    default int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
        return 0;
    }

    @Override
    default String getFileExtension() {
        return null;
    }

    @Override
    default IPath getFullPath() {
        return null;
    }

    @Override
    default long getLocalTimeStamp() {
        return 0;
    }

    @Override
    default IPath getLocation() {
        return null;
    }

    @Override
    default URI getLocationURI() {
        return null;
    }

    @Override
    default IMarker getMarker(long id) {
        return null;
    }

    @Override
    default long getModificationStamp() {
        return 0;
    }

    @Override
    default String getName() {
        return null;
    }

    @Override
    default IPathVariableManager getPathVariableManager() {
        return null;
    }

    @Override
    default IContainer getParent() {
        return null;
    }

    @Override
    default Map<QualifiedName, String> getPersistentProperties() throws CoreException {
        return null;
    }

    @Override
    default String getPersistentProperty(QualifiedName key) throws CoreException {
        return null;
    }

    @Override
    default IProject getProject() {
        return null;
    }

    @Override
    default IPath getProjectRelativePath() {
        return null;
    }

    @Override
    default IPath getRawLocation() {
        return null;
    }

    @Override
    default URI getRawLocationURI() {
        return null;
    }

    @Override
    default ResourceAttributes getResourceAttributes() {
        return null;
    }

    @Override
    default Map<QualifiedName, Object> getSessionProperties() throws CoreException {
        return null;
    }

    @Override
    default Object getSessionProperty(QualifiedName key) throws CoreException {
        return null;
    }

    @Override
    default int getType() {
        return 0;
    }

    @Override
    default IWorkspace getWorkspace() {
        return null;
    }

    @Override
    default boolean isAccessible() {
        return false;
    }

    @Override
    default boolean isDerived() {
        return false;
    }

    @Override
    default boolean isDerived(int options) {
        return false;
    }

    @Override
    default boolean isHidden() {
        return false;
    }

    @Override
    default boolean isHidden(int options) {
        return false;
    }

    @Override
    default boolean isLinked() {
        return false;
    }

    @Override
    default boolean isVirtual() {
        return false;
    }

    @Override
    default boolean isLinked(int options) {
        return false;
    }

    @Override
    default boolean isLocal(int depth) {
        return false;
    }

    @Override
    default boolean isPhantom() {
        return false;
    }

    @Override
    default boolean isReadOnly() {
        return false;
    }

    @Override
    default boolean isSynchronized(int depth) {
        return false;
    }

    @Override
    default boolean isTeamPrivateMember() {
        return false;
    }

    @Override
    default boolean isTeamPrivateMember(int options) {
        return false;
    }

    @Override
    default void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void revertModificationStamp(long value) throws CoreException {

    }

    @Override
    default void setDerived(boolean isDerived) throws CoreException {

    }

    @Override
    default void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default void setHidden(boolean isHidden) throws CoreException {

    }

    @Override
    default void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default long setLocalTimeStamp(long value) throws CoreException {
        return 0;
    }

    @Override
    default void setPersistentProperty(QualifiedName key, String value) throws CoreException {

    }

    @Override
    default void setReadOnly(boolean readOnly) {

    }

    @Override
    default void setResourceAttributes(ResourceAttributes attributes) throws CoreException {

    }

    @Override
    default void setSessionProperty(QualifiedName key, Object value) throws CoreException {

    }

    @Override
    default void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {

    }

    @Override
    default void touch(IProgressMonitor monitor) throws CoreException {

    }

    @Override
    default <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    @Override
    default boolean contains(ISchedulingRule rule) {
        return false;
    }

    @Override
    default boolean isConflicting(ISchedulingRule rule) {
        return false;
    }
}
