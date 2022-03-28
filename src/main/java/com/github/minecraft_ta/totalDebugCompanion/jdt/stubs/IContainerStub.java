package com.github.minecraft_ta.totalDebugCompanion.jdt.stubs;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IContainerStub extends IContainer, IResourceStub {

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
}
