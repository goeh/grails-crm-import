/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package grails.plugins.crm.dataimport

import groovyjarjarasm.asm.Opcodes
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Crm Import AST Transformation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class CrmImportASTTransformation implements ASTTransformation {

    private static final Log LOG = LogFactory.getLog(CrmImportASTTransformation.class)

    private static final MATCH_METHOD = "exists"

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        ExpandoMetaClass.disableGlobally()

        if (!nodes) return
        if (nodes.size() < 2) return
        if (!(nodes[0] instanceof AnnotationNode)) return
        if (!(nodes[1] instanceof ClassNode)) return
        AnnotationNode crmImport = nodes[0]
        ClassNode theClass = nodes[1].getPlainNodeReference()
        Map<String, Expression> members = crmImport.getMembers()
        if (members?.value && !GrailsASTUtils.hasOrInheritsProperty(theClass, MATCH_METHOD)) {
            LOG.debug("Adding method exists() to class " + theClass.getName() + " for domain " + members.value.getType())
            theClass.addMethod(createMatchMethod(members.value.getType()))
        }
        theClass.addMethod(createBindDataMethod1())
        theClass.addMethod(createBindDataMethod2())
    }

    /**
     * Create method match(Map data, Closure matchCriteria) with the following method body:
     * <domainClass>.createCriteria().get(matchCriteria.curry(data))
     *
     * @param domainClass
     * @return
     */
    private MethodNode createMatchMethod(ClassNode domainClass) {
        Statement statement = new BlockStatement(
                [new ExpressionStatement(
                        new MethodCallExpression(
                                new StaticMethodCallExpression(
                                        domainClass,
                                        "createCriteria",
                                        ArgumentListExpression.EMPTY_ARGUMENTS),
                                new ConstantExpression('get'),
                                new ArgumentListExpression(
                                        new MethodCallExpression(
                                                new VariableExpression('matchCriteria'),
                                                new ConstantExpression('curry'),
                                                new ArgumentListExpression(new VariableExpression('data'))
                                        ))
                        )
                )],
                new VariableScope())

        new MethodNode(MATCH_METHOD, Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE.getPlainNodeReference(),
                [
                        new Parameter(ClassHelper.MAP_TYPE.getPlainNodeReference(), 'data'),
                        new Parameter(ClassHelper.CLOSURE_TYPE.getPlainNodeReference(), 'matchCriteria')
                ]
                as Parameter[],
                ClassNode.EMPTY_ARRAY,
                statement)
    }

    private MethodNode createBindDataMethod1() {
        def statement = new AstBuilder().buildFromSpec {
            block {
                returnStatement {
                    methodCall {
                        constructorCall BindDynamicMethod.class, {
                            argumentList {
                            }
                        }
                        constant 'invoke'
                        argumentList {
                            variable 'target'
                            constant 'bind'
                            methodCall {
                                list {
                                    variable 'target'
                                    variable 'params'
                                }
                                constant 'toArray'
                                argumentList {
                                }
                            }
                        }
                    }
                }
            }
        }[0]
        new MethodNode("bindData", Opcodes.ACC_PROTECTED, ClassHelper.OBJECT_TYPE.getPlainNodeReference(),
                [new Parameter(ClassHelper.GROOVY_OBJECT_TYPE.getPlainNodeReference(), 'target'),
                        new Parameter(ClassHelper.MAP_TYPE.getPlainNodeReference(), 'params')]
                as Parameter[],
                ClassNode.EMPTY_ARRAY,
                statement)
    }

    private MethodNode createBindDataMethod2() {
        def statement = new AstBuilder().buildFromSpec {
            block {
                returnStatement {
                    methodCall {
                        constructorCall BindDynamicMethod.class, {
                            argumentList {
                            }
                        }
                        constant 'invoke'
                        argumentList {
                            variable 'target'
                            constant 'bind'
                            methodCall {
                                list {
                                    variable 'target'
                                    variable 'params'
                                    variable 'includesExcludes'
                                }
                                constant 'toArray'
                                argumentList {
                                }
                            }
                        }
                    }
                }
            }
        }[0]
        new MethodNode("bindData", Opcodes.ACC_PROTECTED, ClassHelper.OBJECT_TYPE.getPlainNodeReference(),
                [new Parameter(ClassHelper.OBJECT_TYPE.getPlainNodeReference(), 'target'),
                        new Parameter(ClassHelper.MAP_TYPE.getPlainNodeReference(), 'params'),
                        new Parameter(ClassHelper.MAP_TYPE.getPlainNodeReference(), 'includesExcludes')]
                as Parameter[],
                ClassNode.EMPTY_ARRAY,
                statement)
    }
}
