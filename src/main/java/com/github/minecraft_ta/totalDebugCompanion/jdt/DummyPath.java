package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.runtime.IPath;

import java.io.File;

class DummyPath implements IPath {

    @Override
    public IPath addFileExtension(String extension) {
        return null;
    }

    @Override
    public IPath addTrailingSeparator() {
        return null;
    }

    @Override
    public IPath append(String path) {
        return null;
    }

    @Override
    public IPath append(IPath path) {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public String getDevice() {
        return null;
    }

    @Override
    public String getFileExtension() {
        return null;
    }

    @Override
    public boolean hasTrailingSeparator() {
        return false;
    }

    @Override
    public boolean isAbsolute() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isPrefixOf(IPath anotherPath) {
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isUNC() {
        return false;
    }

    @Override
    public boolean isValidPath(String path) {
        return false;
    }

    @Override
    public boolean isValidSegment(String segment) {
        return false;
    }

    @Override
    public String lastSegment() {
        return null;
    }

    @Override
    public IPath makeAbsolute() {
        return null;
    }

    @Override
    public IPath makeRelative() {
        return null;
    }

    @Override
    public IPath makeRelativeTo(IPath base) {
        return null;
    }

    @Override
    public IPath makeUNC(boolean toUNC) {
        return null;
    }

    @Override
    public int matchingFirstSegments(IPath anotherPath) {
        return 0;
    }

    @Override
    public IPath removeFileExtension() {
        return null;
    }

    @Override
    public IPath removeFirstSegments(int count) {
        return null;
    }

    @Override
    public IPath removeLastSegments(int count) {
        return null;
    }

    @Override
    public IPath removeTrailingSeparator() {
        return null;
    }

    @Override
    public String segment(int index) {
        return null;
    }

    @Override
    public int segmentCount() {
        return 0;
    }

    @Override
    public String[] segments() {
        return new String[0];
    }

    @Override
    public IPath setDevice(String device) {
        return null;
    }

    @Override
    public File toFile() {
        return null;
    }

    @Override
    public String toOSString() {
        return null;
    }

    @Override
    public String toPortableString() {
        return null;
    }

    @Override
    public IPath uptoSegment(int count) {
        return null;
    }
}
