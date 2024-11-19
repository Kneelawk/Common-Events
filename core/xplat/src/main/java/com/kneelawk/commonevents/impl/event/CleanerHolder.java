package com.kneelawk.commonevents.impl.event;

import java.lang.ref.Cleaner;

public class CleanerHolder {
    public static final Cleaner CLEANER = Cleaner.create();
}
