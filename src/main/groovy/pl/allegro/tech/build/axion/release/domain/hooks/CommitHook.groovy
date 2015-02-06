package pl.allegro.tech.build.axion.release.domain.hooks

class CommitHook implements ReleaseHook {
    
    private Closure customAction

    CommitHook(Closure customAction) {
        this.customAction = customAction
    }

    CommitHook() {
        this({v, p -> "Release version: $v"})
    }
    
    @Override
    void act(HookContext hookContext) {
        if(hookContext.patternsToCommit.isEmpty()) {
            // there has to be some pattern, and build.gradle should always exist.. better than * i think
            hookContext.patternsToCommit.add('build.gradle')
        }
        
        String message = customAction(hookContext.currentVersion, hookContext.position)
        hookContext.commit(hookContext.patternsToCommit, message)
    }

    static final class Factory extends DefaultReleaseHookFactory {

        @Override
        ReleaseHook create() {
            return new CommitHook()
        }

        @Override
        ReleaseHook create(Closure customAction) {
            return new CommitHook(customAction)
        }
    }
}