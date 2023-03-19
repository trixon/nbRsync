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
package se.trixon.jotasync.core;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class Progress {

    private String mEta;
    private double mPercentage;
    private String mSize;
    private String mSpeed;
    private boolean mValid;

    public Progress() {
    }

    public String getEta() {
        return mEta;
    }

    public double getPercentage() {
        return mPercentage;
    }

    public String getSize() {
        return mSize;
    }

    public String getSpeed() {
        return mSpeed;
    }

    public boolean isValid() {
        return mValid;
    }

    public boolean parse(String line) {
        mValid = false;
        String[] elements = StringUtils.split(line);

        if (elements.length >= 4
                && elements[1].contains("%")
                && elements[2].contains("/")
                && elements[3].contains(":")) {
            mSize = elements[0];
            mPercentage = Integer.parseInt(elements[1].replace("%", "")) / 100.0;
            mSpeed = elements[2];
            mEta = elements[3];

            mValid = true;
        }

        return mValid;
    }

    @Override
    public String toString() {
        return String.format("%s   %s   %s", mSize, mSpeed, mEta);
    }
}
