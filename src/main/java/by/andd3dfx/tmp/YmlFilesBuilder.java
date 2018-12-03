package by.andd3dfx.tmp;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

public interface YmlFilesBuilder {

    void buildPackageYmlFile(PackageElement packageElement, String outputPath);

    void buildClassYmlFile(TypeElement typeElement, String outputPath);
}