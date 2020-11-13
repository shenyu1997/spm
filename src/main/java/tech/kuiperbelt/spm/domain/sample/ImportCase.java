package tech.kuiperbelt.spm.domain.sample;

public interface ImportCase {
    default String name(){
        return getClass().getSimpleName();
    }

    void importDate();

}
