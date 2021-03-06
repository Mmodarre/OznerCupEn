/**
 * The Bouncy Castle License
 *
 * Copyright (c) 2000-2015 The Legion Of The Bouncy Castle Inc. (http://www.bouncycastle.org)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.aylanetworks.aaml.spongycastle;


/**
 * a holding class for public/private parameter pairs.
 */
public class AsymmetricCipherKeyPair
{
    private AsymmetricKeyParameter    publicParam;
    private AsymmetricKeyParameter    privateParam;

    /**
     * basic constructor.
     *
     * @param publicParam a public key parameters object.
     * @param privateParam the corresponding private key parameters.
     */
    public AsymmetricCipherKeyPair(
        AsymmetricKeyParameter    publicParam,
        AsymmetricKeyParameter    privateParam)
    {
        this.publicParam = publicParam;
        this.privateParam = privateParam;
    }

    /**
     * basic constructor.
     *
     * @param publicParam a public key parameters object.
     * @param privateParam the corresponding private key parameters.
     * @deprecated use AsymmetricKeyParameter
     */
    public AsymmetricCipherKeyPair(
        CipherParameters    publicParam,
        CipherParameters    privateParam)
    {
        this.publicParam = (AsymmetricKeyParameter)publicParam;
        this.privateParam = (AsymmetricKeyParameter)privateParam;
    }

    /**
     * return the public key parameters.
     *
     * @return the public key parameters.
     */
    public AsymmetricKeyParameter getPublic()
    {
        return publicParam;
    }

    /**
     * return the private key parameters.
     *
     * @return the private key parameters.
     */
    public AsymmetricKeyParameter getPrivate()
    {
        return privateParam;
    }
}














