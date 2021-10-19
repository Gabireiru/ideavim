package com.maddyhome.idea.vim.vimscript.model.statements

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.ex.ExException
import com.maddyhome.idea.vim.vimscript.model.Executable
import com.maddyhome.idea.vim.vimscript.model.ExecutionResult
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimDataType
import com.maddyhome.idea.vim.vimscript.model.expressions.Expression
import com.maddyhome.idea.vim.vimscript.model.expressions.Scope
import com.maddyhome.idea.vim.vimscript.services.FunctionStorage

data class FunctionDeclaration(
  val scope: Scope?,
  val name: String,
  val args: List<String>,
  val defaultArgs: List<Pair<String, Expression>>,
  val body: List<Executable>,
  val replaceExisting: Boolean,
  val flags: Set<FunctionFlag>,
  val hasOptionalArguments: Boolean,
) : Executable {
  override lateinit var parent: Executable
  var isDeleted = false

  /**
   * we store the "a:" and "l:" scope variables here
   * see ":h scope"
   */
  val functionVariables: MutableMap<String, VimDataType> = mutableMapOf()
  val localVariables: MutableMap<String, VimDataType> = mutableMapOf()

  override fun execute(editor: Editor, context: DataContext): ExecutionResult {
    val forbiddenArgumentNames = setOf("firstline", "lastline")
    val forbiddenArgument = args.firstOrNull { forbiddenArgumentNames.contains(it) }
    if (forbiddenArgument != null) {
      throw ExException("E125: Illegal argument: $forbiddenArgument")
    }

    body.forEach { it.parent = this }
    FunctionStorage.storeFunction(this)
    return ExecutionResult.Success
  }
}

enum class FunctionFlag(val abbrev: String) {
  RANGE("range"),
  ABORT("abort"),
  DICT("dict"),
  CLOSURE("closure");

  companion object {
    fun getByName(abbrev: String): FunctionFlag? {
      return values().firstOrNull { it.abbrev == abbrev }
    }
  }
}