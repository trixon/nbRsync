/* 
 * Copyright 2023 Patrik Karlström <patrik@trixon.se>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.jotasync.ui.editor.task;

import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum ArgRsync implements ArgBase {
    ACLS("A", "acls"),
    ADDRESS(null, "address=ADDRESS"),
    APPEND(null, "append"),
    APPEND_VERIFY(null, "append-verify"),
    ARCHIVE("a", "archive"),
    BACKUP("b", "backup"),
    BACKUP_DIR(null, "backup-dir=DIR"),
    BLOCKING_IO(null, "blocking-io"),
    BLOCK_SIZE("B", "block-size=SIZE"),
    BWLIMIT(null, "bwlimit=RATE"),
    CHECKSUM("c", "checksum"),
    CHECKSUM_SEED(null, "checksum-seed=NUM"),
    CHMOD(null, "chmod=CHMOD"),
    CHOWN(null, "chown=USER:GROUP"),
    COMPARE_DEST(null, "compare-dest=DIR"),
    COMPRESS("z", "compress"),
    COMPRESS_LEVEL(null, "compress-level=NUM"),
    CONTIMEOUT(null, "contimeout=SECONDS"),
    COPY_DEST(null, "copy-dest=DIR"),
    COPY_DIRLINKS("k", "copy-dirlinks"),
    COPY_LINKS("L", "copy-links"),
    COPY_UNSAFE_LINKS(null, "copy-unsafe-links"),
    CVS_EXLUDE("C", "cvs-exclude"),
    D("D", null),
    DEBUG(null, "debug=FLAGS"),
    DEL(null, "del"),
    DELAY_UPDATES(null, "delay-updates"),
    DELETE(null, "delete"),
    DELETE_AFTER(null, "delete-after"),
    DELETE_BEFORE(null, "delete-before"),
    DELETE_DELAY(null, "delete-delay"),
    DELETE_DURING(null, "delete-during"),
    DELETE_EXCLUDED(null, "delete-excluded"),
    DELETE_MISSING_ARGS(null, "delete-missing-args"),
    DEVICES(null, "devices"),
    DIRS("d", "dirs"),
    DRY_RUN("n", "dry-run"),
    EXECUTABILITY("E", "executability"),
    EXISTING(null, "existing"),
    FILES_FROM(null, "files-from=FILE"),
    FILTER("f", "filter=RULE"),
    FORCE(null, "force"),
    FROM0("0", "from0"),
    FUZZY("y", "fuzzy"),
    GROUPMAP(null, "groupmap=STRING"),
    HARD_LINKS("H", "hard-links"),
    HUMAN_READABLE("h", "human-readable"),
    ICONV(null, "iconv=CONVERT_SPEC"),
    IGNORE_ERRORS(null, "ignore-errors"),
    IGNORE_EXISTING(null, "ignore-existing"),
    IGNORE_MISSING_ARGS(null, "ignore-missing-args"),
    IGNORE_TIMES("I", "ignore-times"),
    INFO(null, "info=FLAGS"),
    INPLACE(null, "inplace"),
    IPV4("4", "ipv4"),
    IPV6("6", "ipv6"),
    ITEMIZE_CHANGES("i", "itemize-changes"),
    KEEP_DIRLINKS("K", "keep-dirlinks"),
    LINKS("l", "links"),
    LINK_DEST(null, "link-dest=DIR"),
    LIST_ONLY(null, "list-only"),
    LOG_FILE(null, "log-file=FILE"),
    LOG_FILE_FORMAT(null, "log-file-format=FMT"),
    MAX_DELETE(null, "max-delete=NUM"),
    MAX_SIZE(null, "max-size=SIZE"),
    MIN_SIZE(null, "min-size=SIZE"),
    MODIFY_WINDOW(null, "modify-window=NUM"),
    MSGS2STDERR(null, "msgs2stderr"),
    MUNGE_LINKS(null, "munge-links"),
    NO_IMPLIED_DIRS(null, "no-implied-dirs"),
    NO_MOTD(null, "no-motd"),
    NUMERIC_IDS(null, "numeric-ids"),
    OMIT_DIR_TIMES("O", "omit-dir-times"),
    OMIT_LINK_TIMES("J", "omit-link-times"),
    ONE_FILE_SYSTEM("x", "one-file-system"),
    ONLY_WRITE_BATCH(null, "only-write-batch=FILE"),
    OUTBUF(null, "outbuf=N|L|B"),
    OUT_FORMAT(null, "out-format=FORMAT"),
    PARTIAL(null, "partial"),
    PARTIAL_DIR(null, "partial-dir=DIR"),
    PARTIAL_PROGRESS("P", null),
    PASSWORD_FILE(null, "password-file=FILE"),
    PORT(null, "port=PORT"),
    PREALLOCATE(null, "preallocate"),
    PRESERVE_GROUP("g", "group"),
    PRESERVE_OWNER("o", "owner"),
    PRESERVE_PERMISSION("p", "perms"),
    PRESERVE_TIME("t", "times"),
    PROGRESS(null, "progress"),
    PROTECT_ARGS("s", "protect-args"),
    PROTOCOL(null, "protocol=NUM"),
    PRUNE_EMPTY_DIRS("m", "prune-empty-dirs"),
    QUIET("q", "quiet"),
    READ_BATCH(null, "read-batch=FILE"),
    RECURSIVE("r", "recursive"),
    RELATIVE("R", "relative"),
    REMOTE_OPTION("M", "remote-option=OPTION"),
    REMOVE_SOURCE_FILES(null, "remove-source-files"),
    RSH("e", "rsh=COMMAND"),
    RSYNC_PATH(null, "rsync-path=PROGRAM"),
    SAFE_LINKS(null, "safe-links"),
    SIZE_ONLY(null, "size-only"),
    SKIP_COMPRESS(null, "skip-compress=LIST"),
    SOCKOPTS(null, "sockopts=OPTIONS"),
    SPARSE("s", "sparse"),
    SPECIALS(null, "specials"),
    STATS(null, "stats"),
    SUFFIX(null, "suffix=SUFFIX"),
    SUPER(null, "super"),
    TEMP_DIR("T", "temp-dir=DIR"),
    TIMEOUT(null, "timeout=SECONDS"),
    UPDATE("u", "update"),
    USERMAP(null, "usermap=STRING"),
    VERBOSE("v", "verbose"),
    WHOLE_FILE("W", "whole-file"),
    WRITE_BATCH(null, "write-batch=FILE"),
    _8_BIT_OUTPUT("8", "8-bit-output");

    private final ResourceBundle mBundle = SystemHelper.getBundle(ArgRsync.class, "ArgRsync");
    private final String mLongArg;
    private final String mShortArg;
    private final String mTitle;
    private String mDynamicArg;

    private ArgRsync(String shortArg, String longArg) {
        mShortArg = shortArg;
        mLongArg = longArg;
        String key = name();
        mTitle = mBundle.containsKey(key) ? mBundle.getString(key) : "_MISSING DESCRIPTION " + key;
    }

    @Override
    public boolean filter(String filter) {
        return getShortArg().toLowerCase().contains(filter.toLowerCase())
                || getLongArg().toLowerCase().contains(filter.toLowerCase())
                || mTitle.toLowerCase().contains(filter.toLowerCase());
    }

    @Override
    public String getArg() {
        if (mLongArg != null) {
            return getLongArg();
        } else {
            return getShortArg();
        }
    }

    @Override
    public String getDynamicArg() {
        return mDynamicArg;
    }

    @Override
    public String getLongArg() {
        String result;
        if (mLongArg != null) {
            result = "--" + mLongArg;
        } else {
            result = "";
        }

        if (result.contains("=")) {
            String[] elements = StringUtils.split(result, "=", 2);
            String prefix = elements[0];
            result = String.format("%s=%s", prefix, mDynamicArg == null ? elements[1] : mDynamicArg);
        }

        return result;
    }

    @Override
    public String getShortArg() {
        if (mShortArg != null) {
            return "-" + mShortArg;
        } else {
            return "";
        }
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public static void reset() {
        for (ArgRsync value : ArgRsync.values()) {
            value.setDynamicArg(null);
        }
    }

    @Override
    public void setDynamicArg(String dynamicArg) {
        mDynamicArg = dynamicArg;
    }
}
