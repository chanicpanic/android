/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

/**
 * Subclasses of this unchecked Exception are thrown to stop execution of AI logic
 */
abstract class EndException : RuntimeException()

class EndGameException : EndException()