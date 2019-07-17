/*
 * Copyright 2019 Cyface GmbH
 *
 * This file is part of the Cyface SDK for Android.
 *
 * The Cyface SDK for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Cyface SDK for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Cyface SDK for Android. If not, see <http://www.gnu.org/licenses/>.
 */
package de.cyface.app.energy_settings;

/**
 * A runtime exception thrown when a pre- or post condition check fails.
 *
 * @author Armin Schnabel
 * @version 1.0.0
 * @since 1.0.0
 */
class ValidationException extends RuntimeException {
    /**
     * Creates a new completely initialized {@code ValidationException} with a message explaining further details about
     * the reasons of this exception.
     *
     * @param detailMessage Provides a detailed explanation about this exception.
     */
    ValidationException(final String detailMessage) {
        super(detailMessage);
    }
}
